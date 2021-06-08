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

package eu.vironlab.vironcloud.common.service

import eu.vironlab.vextension.lang.Nameable
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class DefaultServiceRepository : ServiceRepository {

    private val services: MutableMap<KClass<*>, MutableCollection<RepositoryEntry<*>>> = ConcurrentHashMap()

    internal inner class RepositoryEntry<T>(override val name: String, val value: T) : Nameable

    override fun <T : Any> getService(serviceClass: KClass<T>): T? = this.services[serviceClass]?.firstOrNull()?.value as T?

    override fun <T : Any> getService(serviceClass: KClass<T>, name: String) =
        this.services[serviceClass]?.filter { it.name == name }?.firstOrNull()?.value as T?

    override fun <T : Any, I : T> registerService(name: String, serviceClass: KClass<T>, implementation: I): I {
        val entry = this.services[serviceClass] ?: mutableListOf()
        entry.add(RepositoryEntry(name, implementation))
        this.services[serviceClass] = entry
        return implementation
    }

    override fun unregister(serviceClass: KClass<*>, name: String) =
        this.services[serviceClass]?.removeAll { it.name == name }

    override fun <T : Any, I : T> unregister(serviceClass: KClass<T>, implementationClass: KClass<I>) =
        this.services[serviceClass]?.removeAll { it.value?.javaClass == implementationClass }

}