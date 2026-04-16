rootProject.name = "grpc-checkout"

include("user")
include("payment")
include("coupon")
include("product")
include("checkout")
include("proto")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}