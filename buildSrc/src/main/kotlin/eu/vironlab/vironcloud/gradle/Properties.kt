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

package eu.vironlab.vironcloud.gradle

import eu.vironlab.vironcloud.gradle.SerializedProperties

object Properties {

    @JvmStatic
    val modules: MutableList<String> = mutableListOf()

    @JvmStatic
    val group = "eu.vironlab.vironcloud"

    @JvmStatic
    val version = "1.0.0-SNAPSHOT"

    @JvmStatic
    val repositories = arrayListOf<String>(
        "https://dl.bintray.com/kotlin/kotlinx/",
        "https://dl.bintray.com/kotlin/ktor/",
        "https://repo1.maven.org/maven2/",
        "https://oss.sonatype.org/content/repositories/snapshots/",
        "https://repo.vironlab.eu/repository/maven-snapshot/"
    )

    @JvmStatic
    val minecraftRepositories = arrayListOf<String>(
        "https://repo.spongepowered.org/maven/",
        "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
        "https://oss.sonatype.org/content/repositories/snapshots/",
        "https://repo.velocitypowered.com/snapshots/"

    )

    @JvmStatic
    val versions: MutableMap<String, String> = mutableMapOf<String, String>().also {
        it["kotlin"] = "1.5.10"
        it["vextension"] = "2.0.0-SNAPSHOT"
    }

    @JvmStatic
    val dependencies: MutableMap<String, MutableMap<String, String>> =
        mutableMapOf<String, MutableMap<String, String>>().also {
            it["kotlin"] = mutableMapOf(
                Pair("stdlib", "org.jetbrains.kotlin:kotlin-stdlib:%version%"),
                Pair("serialization", "org.jetbrains.kotlin:kotlin-serialization:%version%")
            )
            it["google"] = mutableMapOf(
                Pair("gson", "com.google.code.gson:gson:2.8.6"),
                Pair("guice", "com.google.inject:guice:5.0.1"),
                Pair("guava", "com.google.guava:guava:30.1.1-jre"),
                Pair("guava-failureaccess", "com.google.guava:failureaccess:1.0.1")
            )
            it["javax"] = mutableMapOf(
                Pair("inject", "javax.inject:javax.inject:1")
            )
            it["aopalliance"] = mutableMapOf(
                Pair("aopalliance", "aopalliance:aopalliance:1.0")
            )
            it["vextension"] = mutableMapOf(
                Pair("common", "eu.vironlab.vextension:vextension-common:%version%")
            )
            it["kotlinx"] = mutableMapOf(
                Pair("coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
            )
            it["lettuce"] = mutableMapOf(
                Pair("core", "io.lettuce:lettuce-core:6.1.0.RELEASE")
            )
        }

    @JvmStatic
    fun serialize(): SerializedProperties {
        val depend: MutableList<String> = mutableListOf()
        dependencies.keys.forEach { key ->
            val entries = dependencies[key]!!
            entries.keys.forEach { entryKey ->
                depend.add(getDependency(key, entryKey))
            }
        }
        return SerializedProperties(
            repositories,
            modules,
            depend,
            version
        )
    }

}