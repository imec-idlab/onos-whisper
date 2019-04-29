COMPILE_DEPS = CORE_DEPS + JACKSON + NETTY + KRYO + REST + CLI + [
    "//protocols/whisper/api:onos-protocols-whisper-api",
    "//protocols/whisper/datamodel:onos-protocols-whisper-datamodel",
    "//core/store/serializers:onos-core-serializers",
    "@io_netty_netty//jar",
]

osgi_jar(
    api_description = "REST API for Whisper",
    api_package = "org.onosproject.whisper.rest",
    api_title = "Whisper API",
    api_version = "1.0",
    karaf_command_packages = ["org.onosproject.whisper"],
    web_context = "/onos/whisper",
    deps = COMPILE_DEPS,
)

