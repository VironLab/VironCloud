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

object Properties {

    @JvmStatic
    val modules: MutableList<String> = mutableListOf()

    @JvmStatic
    val group = "eu.vironlab.vironcloud"

    @JvmStatic
    val version = "1.0.0-SNAPSHOT"

    @JvmStatic
    val repositories = arrayListOf<String>(
        Repositories.KOTLINX,
        Repositories.KTOR,
        Repositories.MAVEN_CENTRAL,
        Repositories.SONATYPE,
        Repositories.VIRONLAB_SNAPSHOT
    )

    @JvmStatic
    val minecraftRepositories = arrayListOf<String>(
        Repositories.MINECRAFT_SPIGOT,
        Repositories.MINECRAFT_SPONGE,
        Repositories.MINECRAFT_VELOCITY
    )

    @JvmStatic
    val versions: MutableMap<String, String> = mutableMapOf<String, String>().also {
        it["kotlin"] = "1.5.10"
        it["vextension"] = "2.0.0-SNAPSHOT"
    }

    @JvmStatic
    val dependencies: MutableMap<String, MutableMap<String, Pair<Boolean, String>>> =
        mutableMapOf<String, MutableMap<String, Pair<Boolean, String>>>().also {
            it["kotlin"] = mutableMapOf(
                Pair("stdlib", Pair(true, "org.jetbrains.kotlin:kotlin-stdlib:%version%")),
                Pair("serialization", Pair(true, "org.jetbrains.kotlin:kotlin-serialization:%version%"))
            )
            it["google"] = mutableMapOf(
                Pair("gson", Pair(true, "com.google.code.gson:gson:2.8.6")),
                Pair("guice", Pair(false, "com.google.inject:guice:5.0.1")),
                Pair("guava", Pair(false, "com.google.guava:guava:30.1.1-jre")),
                Pair("guava-failureaccess", Pair(false, "com.google.guava:failureaccess:1.0.1"))
            )
            it["javax"] = mutableMapOf(
                Pair("inject", Pair(false, "javax.inject:javax.inject:1"))
            )
            it["aopalliance"] = mutableMapOf(
                Pair("aopalliance", Pair(false, "aopalliance:aopalliance:1.0"))
            )
            it["vextension"] = mutableMapOf(
                Pair("common", Pair(true, "eu.vironlab.vextension:vextension-common:%version%"))
            )
            it["kotlinx"] = mutableMapOf(
                Pair("coroutines-core", Pair(true, "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3"))
            )
            it["lettuce"] = mutableMapOf(
                Pair("core", Pair(true, "io.lettuce:lettuce-core:6.1.0.RELEASE"))
            )
        }

    @JvmStatic
    fun serializeForClient(): SerializedProperties {
        val depend: MutableList<String> = mutableListOf()
        dependencies.keys.forEach { key ->
            val entries = dependencies[key]!!
            entries.filterValues { it.first }.keys.forEach { entryKey ->
                depend.add(getDependency(key, entryKey))
            }
        }
        return SerializedProperties(
            repositories,
            depend,
            version
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