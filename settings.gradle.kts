rootProject.name = "grpc-checkout"

include("user")
include("payment")
include("coupon")
include("product")
include("checkout")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}