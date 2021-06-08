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

package eu.vironlab.vironcloud.extension.annotation

import com.google.gson.GsonBuilder
import eu.vironlab.vironcloud.extension.ExtensionMeta
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

class ExtensionProcessor : AbstractProcessor() {

    val GSON = GsonBuilder().serializeNulls().setPrettyPrinting().create()
    private var running: Boolean = false
    private lateinit var filer: Filer
    private lateinit var processingEnvironment: ProcessingEnvironment

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        processingEnvironment = processingEnv
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            if (!running) {
                println("________________________________________________________________________________")
                println(
                    "___    _______                        _______________               _________\n" +
                            "__ |  / /___(_)______________ _______ __  ____/___  /______ ____  ________  /\n" +
                            "__ | / / __  / __  ___/_  __ \\__  __ \\_  /     __  / _  __ \\_  / / /_  __  / \n" +
                            "__ |/ /  _  /  _  /    / /_/ /_  / / // /___   _  /  / /_/ // /_/ / / /_/ /  \n" +
                            "_____/   /_/   /_/     \\____/ /_/ /_/ \\____/   /_/   \\____/ \\__,_/  \\__,_/   \n" +
                            "                                                                             "
                )
                println("Starting VironCloud Extension Processor")
                this.running = true
                for (element: Element in roundEnv.getElementsAnnotatedWith(VironCloudExtension::class.java)) {
                    if (!element.kind.isClass) {
                        println("Only Classes can be added as VironCloudExtension")
                    }
                    val mainClassName: String = (element as TypeElement).qualifiedName.toString()
                    val extension = element.getAnnotation(VironCloudExtension::class.java)
                    val dependencyManagement = element.getAnnotation(ExtensionDependencyManagement::class.java)
                    val dependencies: MutableList<String> = mutableListOf()
                    val repos: MutableMap<String, String> = mutableMapOf()
                    if (dependencyManagement != null) {
                        dependencyManagement.dependencies.forEach { depend ->
                            dependencies.add("${depend.repo}:${depend.group}:${depend.artifact}:${depend.version}")
                        }
                        dependencyManagement.repositories.forEach { repo ->
                            repos[repo.name] = repo.url
                        }
                    }
                    val meta = ExtensionMeta(
                        extension.name,
                        extension.author,
                        extension.description,
                        extension.website,
                        mainClassName,
                        extension.reloadable,
                        dependencies,
                        repos
                    )
                    val file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "vironcloud-extension.json")
                    file.openWriter().also { writer ->
                        writer.write(GSON.toJson(meta))
                        writer.flush()
                        writer.close()
                    }
                    println("Created vironcloud-extension.json File for module: ${extension.name}")
                }
                println("________________________________________________________________________________")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(VironCloudExtension::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion? {
        return SourceVersion.latestSupported()
    }


}