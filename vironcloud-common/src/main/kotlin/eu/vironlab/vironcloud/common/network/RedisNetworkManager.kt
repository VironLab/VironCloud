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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import eu.vironlab.vextension.concurrent.task.QueuedTask
import eu.vironlab.vextension.concurrent.task.queueTask
import eu.vironlab.vextension.document.Document
import eu.vironlab.vextension.document.documentFromJson
import eu.vironlab.vextension.extension.random
import eu.vironlab.vironcloud.common.network.config.NetworkConnectionConfig
import eu.vironlab.vironcloud.common.network.packet.Packet
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class RedisNetworkManager(val networkConfiguration: NetworkConnectionConfig, val subscribe: List<String>) :
    NetworkManager {

    companion object {
        @JvmStatic
        val GSON: Gson = GsonBuilder().serializeNulls().create()
    }

    val redis = RedisClient.create(
        RedisURI.create(networkConfiguration.host, networkConfiguration.port)
            .also { it.password = networkConfiguration.password.toCharArray() })
        ?: throw IllegalStateException("Cannot create Redis Client")
    val pubSub: StatefulRedisPubSubConnection<String, String>
    val sync: RedisPubSubCommands<String, String>
    val async: RedisPubSubAsyncCommands<String, String>
    val packets: MutableMap<String, Packet<*>> = ConcurrentHashMap()
    val queries: MutableMap<String, Any> = ConcurrentHashMap()
    val awaitingQueries: MutableMap<String, KClass<*>> = ConcurrentHashMap()
    val subscribeListener: MutableMap<String, (Document) -> Unit> = ConcurrentHashMap()

    init {
        redis.connectPubSub()
        this.pubSub = redis.connectPubSub().also {
            it.addListener(RedisListener())
        }
        this.async = pubSub.async()
        this.sync = pubSub.sync()
        for (entry in subscribe) {
            sync.subscribe("sync::$entry")
            async.subscribe("async::$entry")
        }
    }

    override fun <T> registerPacket(packet: Packet<T>): NetworkManager {
        this.packets[packet.javaClass.canonicalName] = packet
        return this
    }

    override fun sendPacketToGroupSync(service: InternalConnection, packet: Packet<*>) =
        sync.publish(service.group, packet.serialize()).let { Unit }

    override fun sendPacketGroupAsync(service: InternalConnection, packet: Packet<*>) =
        async.publish(service.group, packet.serialize()).let { Unit }

    override fun sendPacketSync(service: InternalConnection, packet: Packet<*>) =
        sync.publish(service.name, packet.serialize()).let { Unit }

    override fun sendPacketAsync(service: InternalConnection, packet: Packet<*>) =
        async.publish(service.name, packet.serialize()).let { Unit }

    override fun <T : Any> sendQuery(
        connection: InternalConnection,
        query: Packet<T>,
        answer: KClass<T>
    ): QueuedTask<T?> =
        queueTask<T?> {
            TODO("Add timeout -> Update QueueTask on Vextension")
            var id = String.random(16)
            while (this.awaitingQueries.contains(id)) {
                id = String.random(16)
            }
            this.awaitingQueries[id] = answer
            this.sync.subscribe(id)
            this.sync.publish(connection.name, query.document.append("query", true).append("id", id).toJson())
            while (!this.queries.containsKey(id)) {
                doNothing()
            }
            val rs = this.queries[id] as? T
            this.queries.remove(id)
            this.sync.unsubscribe(id)
            return@queueTask rs
        }

    override fun subscribeSync(channel: String, action: (Document) -> Unit): NetworkManager {
        sync.subscribe(channel)
        subscribeListener[channel] = action
        return this
    }

    override fun subscribeAsync(channel: String, action: (Document) -> Unit): NetworkManager {
        async.subscribe(channel)
        subscribeListener[channel] = action
        return this
    }

    private fun doNothing() {}

    override fun unsubscribe(channel: String) = this.subscribeListener.remove(channel).let { Unit }

    override fun isChannelRegistered(channel: String): Boolean = this.subscribeListener.containsKey(channel)


    internal inner class RedisListener : RedisPubSubAdapter<String, String>() {
        override fun message(channel: String, message: String) {
            if (awaitingQueries.containsKey(channel)) {
                queries[channel] = GSON.fromJson(message, awaitingQueries[channel]!!.java)
                awaitingQueries.remove(channel)
                return
            }
            val data = documentFromJson(message)
            if (subscribeListener.containsKey(channel)) {
                subscribeListener[channel]!!.invoke(data)
                return
            }
        }
    }

}