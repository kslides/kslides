VERSION := $(shell sed -n 's/^version[[:space:]]*=[[:space:]]*//p' gradle.properties | head -1)

ifeq ($(strip $(VERSION)),)
$(error Could not determine project version from gradle.properties)
endif

.PHONY: default build-all stop clean build local-build lint refresh tests \
        fatjar uber dist stage tree process-resources versioncheck kdocs \
        clean-docs site publish-local publish-local-snapshot publish-snapshot \
        publish-maven-central upgrade-wrapper

default: versioncheck

build-all:
	$(MAKE) clean
	$(MAKE) stage

stop:
	./gradlew --stop

clean:
	./gradlew clean

build: clean
	./gradlew build -xtest

local-build: clean
	./gradlew build -PuseMavenLocal=true -xtest

lint:
	./gradlew lintKotlinMain lintKotlinTest

refresh:
	./gradlew --refresh-dependencies build -xtest

tests:
	./gradlew cleanTest test

fatjar:
	./gradlew buildFatJar

uber: fatjar
	java -jar kslides-examples/build/libs/kslides.jar

dist:
	./gradlew installDist

stage:
	./gradlew stage

tree:
	./gradlew -q dependencies

process-resources:
	./gradlew :kslides-core:processResources

versioncheck:
	./gradlew dependencyUpdates --no-configuration-cache --no-parallel

kdocs:
	./gradlew :dokkaGenerate

clean-docs:
	rm -rf docs/playground docs/letsPlot docs/kroki
	rm -rf website/kslides/site website/kslides/.cache

site: clean-docs
	cd website/kslides && uv run zensical serve

publish-local:
	./gradlew publishToMavenLocal

publish-local-snapshot:
	./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenLocal

GPG_ENV = \
	ORG_GRADLE_PROJECT_signingInMemoryKey="$$(gpg --armor --export-secret-keys $$GPG_SIGNING_KEY_ID)" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyId="$$GPG_SIGNING_KEY_ID" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=$$(security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w)

publish-snapshot:
	$(GPG_ENV) ./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenCentral

publish-maven-central:
	$(GPG_ENV) ./gradlew publishAndReleaseToMavenCentral

upgrade-wrapper:
	./gradlew wrapper --gradle-version=9.5.0 --distribution-type=bin
