package eu.vironlab.vironcloud.gradle

object Publish {

    @JvmStatic
    val enabled = System.getProperty("publishName") != null && System.getProperty("publishPassword") != null

    @JvmStatic
    val username = System.getProperty("userName") ?: throw IllegalStateException("Cannot get Username without System Property")

    @JvmStatic
    val password = System.getProperty("userName") ?: throw IllegalStateException("Cannot get Username without System Property")

}