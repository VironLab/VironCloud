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

package eu.vironlab.vironcloud

import eu.vironlab.vextension.document.wrapper.ConfigDocument
import eu.vironlab.vironcloud.common.network.NetworkManager
import eu.vironlab.vironcloud.common.network.config.NetworkConnectionConfig
import eu.vironlab.vironcloud.extension.DefaultExtensionManager
import eu.vironlab.vironcloud.extension.ExtensionManager
import java.io.File
import java.net.URLClassLoader

class VironCloud(val args: Array<String>, val version: String, val extensionDir: File, val classLoader: URLClassLoader) :
    CloudAPI() {

    companion object {
        @JvmStatic
        fun start(args: Array<String>, version: String, extensionDir: File, classLoader: URLClassLoader) =
            VironCloud(args, version, extensionDir, classLoader)
    }

    val homeDir: File = File(System.getProperty("homeDir") ?: System.getProperty("user.dir"))
    val config: ConfigDocument = ConfigDocument(File("config.json")).also {
        it.loadConfig()
        it.get("redis", NetworkConnectionConfig::class.java, NetworkConnectionConfig())
    }
    val extensionManager: ExtensionManager

    init {
        this.extensionManager = DefaultExtensionManager(this).also { manager ->
            this.serviceRepository.registerService("extensionManager", ExtensionManager::class, manager)
            manager.loadAllExtensions(extensionDir)
        }
    }

    override val networkManager: NetworkManager
        get() = TODO("Not yet implemented")


}