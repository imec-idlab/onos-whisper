COMPILE_DEPS = CORE_DEPS + JACKSON + [
    "//protocols/whisper/api:onos-protocols-whisper-api",
    "//protocols/whisper/app:onos-protocols-whisper-app",
    "//protocols/whisper/datamodel:onos-protocols-whisper-datamodel",
]

BUNDLES = JACKSON + [
    "//protocols/whisper/api:onos-protocols-whisper-api",
    "//protocols/whisper/app:onos-protocols-whisper-app",
    "//protocols/whisper/datamodel:onos-protocols-whisper-datamodel",
    ":onos-providers-whisper",
]

TEST_DEPS = TEST_ADAPTERS + [
    "//utils/osgi:onlab-osgi-tests",
]

osgi_jar_with_tests(
    test_deps = TEST_DEPS,
    deps = COMPILE_DEPS,
)

onos_app(
    app_name = 'org.onosproject.whisper',
    category = "Provider",
    description = "Whisper provider to interact with the Whisper REST API",
    title = "Whisper Provider Suite",
    url = "http://idlab.org",
    included_bundles = BUNDLES,
)


