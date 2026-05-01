VERSION := $(shell sed -n 's/^version[[:space:]]*=[[:space:]]*//p' gradle.properties | head -1)

ifeq ($(strip $(VERSION)),)
$(error Could not determine project version from gradle.properties)
endif

.PHONY: default build-all stop clean build local-build lint refresh tests \
        fatjar uber dist stage deps process-resources versioncheck kdocs \
        clean-docs site publish-local publish-local-snapshot check-gpg-env \
        publish-snapshot publish-maven-central upgrade-wrapper

default: versioncheck

build-all: clean stage

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

deps:
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

check-gpg-env:
	@if [ -z "$$GPG_SIGNING_KEY_ID" ]; then \
		echo "Error: GPG_SIGNING_KEY_ID is not set" >&2; exit 1; \
	fi
	@if ! gpg --list-secret-keys "$$GPG_SIGNING_KEY_ID" >/dev/null 2>&1; then \
		echo "Error: no GPG secret key found for GPG_SIGNING_KEY_ID=$$GPG_SIGNING_KEY_ID" >&2; exit 1; \
	fi
	@if ! security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w >/dev/null 2>&1; then \
		echo "Error: keychain entry 'gradle-signing-password' (account 'gpg-signing') not found" >&2; exit 1; \
	fi

# publish-snapshot stages a -SNAPSHOT build to the Sonatype snapshots repo (vanniktech routes
# -SNAPSHOT versions there automatically). publish-maven-central uses the *AndRelease* variant
# to both stage and auto-release the version on the Central Portal — do not "fix" the asymmetry.
publish-snapshot: check-gpg-env
	$(GPG_ENV) ./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenCentral

publish-maven-central: check-gpg-env
	$(GPG_ENV) ./gradlew publishAndReleaseToMavenCentral

upgrade-wrapper:
	./gradlew wrapper --gradle-version=9.5.0 --distribution-type=bin
