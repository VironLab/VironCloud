/**
 *   Copyright © 2020 | vironlab.eu | All Rights Reserved.<p>
 * <p>
 *      ___    _______                        ______         ______  <p>
 *      __ |  / /___(_)______________ _______ ___  / ______ ____  /_ <p>
 *      __ | / / __  / __  ___/_  __ \__  __ \__  /  _  __ `/__  __ \<p>
 *      __ |/ /  _  /  _  /    / /_/ /_  / / /_  /___/ /_/ / _  /_/ /<p>
 *      _____/   /_/   /_/     \____/ /_/ /_/ /_____/\__,_/  /_.___/ <p>
 *<p>
 *    ____  _______     _______ _     ___  ____  __  __ _____ _   _ _____ <p>
 *   |  _ \| ____\ \   / / ____| |   / _ \|  _ \|  \/  | ____| \ | |_   _|<p>
 *   | | | |  _|  \ \ / /|  _| | |  | | | | |_) | |\/| |  _| |  \| | | |  <p>
 *   | |_| | |___  \ V / | |___| |__| |_| |  __/| |  | | |___| |\  | | |  <p>
 *   |____/|_____|  \_/  |_____|_____\___/|_|   |_|  |_|_____|_| \_| |_|  <p>
 *<p>
 *<p>
 *   This program is free software: you can redistribute it and/or modify<p>
 *   it under the terms of the GNU General Public License as published by<p>
 *   the Free Software Foundation, either version 3 of the License, or<p>
 *   (at your option) any later version.<p>
 *<p>
 *   This program is distributed in the hope that it will be useful,<p>
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of<p>
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<p>
 *   GNU General Public License for more details.<p>
 *<p>
 *   You should have received a copy of the GNU General Public License<p>
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.<p>
 *<p>
 *   Creation: Dienstag 08 Juni 2021 21:29:09<p>
 *<p>
 *   Contact:<p>
 *<p>
 *     Discordserver:   https://discord.gg/wvcX92VyEH<p>
 *     Website:         https://vironlab.eu/ <p>
 *     Mail:            contact@vironlab.eu<p>
 *<p>
 */

package eu.vironlab.vironcloud.extension

import com.google.gson.Gson
import com.google.inject.Guice
import eu.vironlab.vironcloud.VironCloud
import eu.vironlab.vironcloud.extension.annotation.ExtensionTask
import eu.vironlab.vironcloud.extension.dependency.ExtensionDependencyUtil
import eu.vironlab.vironcloud.extension.inject.VironCloudExtensionModule
import java.io.File
import java.io.InputStreamReader
import java.lang.reflect.Method
import java.util.jar.JarFile

class DefaultExtensionManager(val cloud: VironCloud) : ExtensionManager {

    val extensions: MutableList<LoadedExtension> = mutableListOf()

    override fun loadExtension(file: File): LoadedExtension {
        val jarFile = JarFile(file)
        val metaEntry = jarFile.getJarEntry("vironcloud-extension.json")
            ?: throw IllegalStateException("Cannot find vironcloud-extension.json in ${file.name}")
        val meta: ExtensionMeta =
            Gson().fromJson(InputStreamReader(jarFile.getInputStream(metaEntry)), ExtensionMeta::class.java)
        jarFile.close()
        val injector = Guice.createInjector(VironCloudExtensionModule(cloud, meta))
        val urls = mutableListOf(file.toURI().toURL()).also {
            it.addAll(
                ExtensionDependencyUtil.getUrls(
                    meta.dependencies,
                    meta.repositories
                )
            )
        }
        val extensionClass = ExtensionClassLoader(urls.toTypedArray(), cloud.classLoader).loadClass(meta.mainClass)
            ?: throw ClassNotFoundException("Main Class of module ${meta.name}: ${meta.mainClass} not Found")
        val extension = injector.getInstance(extensionClass)
        val loadedMeta = LoadedExtension.fromMeta(meta, extension)
        this.extensions += loadedMeta
        return loadedMeta
    }

    override fun <T> getExtension(clazz: Class<T>): T? =
        this.extensions.filter { it.extension.javaClass.canonicalName == clazz.canonicalName }
            .firstOrNull()?.extension as T

    override fun reloadAllExtensions() {
        val reloadable = extensions.filter { it.reloadable }
        if (reloadable.isEmpty()) {
            for (extension in reloadable) {
                executeState(extension, ExtensionState.STOP)
                executeState(extension, ExtensionState.START)
            }
        }
    }

    override fun loadAndStartExtension(file: File): LoadedExtension {
        val meta = loadExtension(file)
        executeState(meta, ExtensionState.START)
        return meta
    }

    override fun startExtensions() = executeState(ExtensionState.START)

    private fun executeState(extension: LoadedExtension, state: ExtensionState) {
        val methods = extension.javaClass.methods.filter { it.isAnnotationPresent(ExtensionTask::class.java) }
            .filter { it.getAnnotation(ExtensionTask::class.java).state == state }
            .sortedWith(object : Comparator<Method> {
                override fun compare(o1: Method, o2: Method): Int =
                    o1.getAnnotation(ExtensionTask::class.java).priority - o2.getAnnotation(ExtensionTask::class.java).priority
            })
        for (method in methods) {
            method.invoke(extension.extension)
        }
    }

    private fun executeState(state: ExtensionState) {
        for (extension in extensions) {
            executeState(extension, state)
        }
    }

    override fun stopExtensions() = executeState(ExtensionState.STOP)
}