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

package eu.vironlab.vironcloud.common.network

import eu.vironlab.vironcloud.common.network.config.NetworkConnectionConfig
import eu.vironlab.vironcloud.common.network.packet.Packet
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands
import java.util.concurrent.ConcurrentHashMap

class RedisNetworkManager(val networkConfiguration: NetworkConnectionConfig) : NetworkManager {

    val redis = RedisClient.create(
        RedisURI.create(networkConfiguration.host, networkConfiguration.port)
            .also { it.password = networkConfiguration.password.toCharArray() })
    val pubSub: StatefulRedisPubSubConnection<String, String>
    val sync: RedisPubSubCommands<String, String>
    val async: RedisPubSubAsyncCommands<String, String>
    val packets: MutableMap<String, Packet> = ConcurrentHashMap()

    init {
        redis.connectPubSub()
        this.pubSub = redis.connectPubSub().also {
            it.addListener(Listener())
        }
        this.async = pubSub.async()
        this.sync = pubSub.sync()
    }

    override fun registerPacketSync(packet: Packet) {
        sync.subscribe(packet.javaClass.canonicalName)
        addPacketToListener(packet)
    }

    override fun registerPacketAsync(packet: Packet) {
        async.subscribe(packet.javaClass.canonicalName)
        addPacketToListener(packet)
    }

    internal fun addPacketToListener(packet: Packet) {
        this.packets[packet.javaClass.canonicalName] = packet
    }

    override fun sendPacketSync(connection: Connection, packet: Packet) {
        TODO("Not yet implemented")
    }

    override fun sendPacketAsync(connection: Connection, packet: Packet) {
        TODO("Not yet implemented")
    }

    internal inner class Listener : RedisPubSubAdapter<String, String>() {
        override fun message(channel: String, message: String) {
            super.message(channel, message)
        }
    }


}