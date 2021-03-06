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

package eu.vironlab.vironcloud.extension.dependency

import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object ExtensionDependencyUtil {

    val libDir: File = File(".libs")

    fun getUrls(dependencies: List<String>, repos: Map<String, String>): MutableList<URL> {
        val rs = mutableListOf<URL>()
        for (dependency in dependencies) {
            val split = dependency.split(":").toTypedArray()
            val depend = Dependency(split[1], split[2], split[3], split[0])
            val filePath: String =
                depend.groupId.replace('.', '/') + '/' + depend.artifactId + '/' + depend.version
            val fileName: String = depend.artifactId + '-' + depend.version + ".jar"
            val folder: File = File(libDir, filePath)
            val dest = File(folder, fileName)
            val url: URL =
                URL(
                    "${
                        repos[depend.repo]!!.let {
                            if (!it.endsWith("/")) {
                                it.plus("/")
                            }
                            it
                        }
                    }$filePath/$fileName"
                )
            if (!dest.exists() || dest.name.toLowerCase().contains("snapshot")) {
                if (folder != null) {
                    if (!folder!!.exists()) {
                        Files.createDirectories(folder!!.toPath())
                    }
                }
                val stream = url.openStream()
                Files.copy(stream, dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
                stream.close()
            }
        }
        return rs
    }


    internal class Dependency(val groupId: String, val artifactId: String, val version: String, val repo: String)

}