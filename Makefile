.PHONY: default help build-all stop clean build local-build lint detekt refresh tests \
        fatjar uber dist stage deps process-resources versioncheck kdocs \
        clean-docs site publish-local publish-local-snapshot publish-snapshot publish-maven-central \
        upgrade-wrapper _check-gpg-env _require-version _require-gradle-version

VERSION := $(shell sed -n 's/^version[[:space:]]*=[[:space:]]*//p' gradle.properties | head -1)
GRADLE_VERSION := $(shell sed -n 's/^gradle[[:space:]]*=[[:space:]]*"\([^"]*\)".*/\1/p' gradle/libs.versions.toml | head -1)

GPG_ENV = \
	ORG_GRADLE_PROJECT_signingInMemoryKey="$$(gpg --armor --export-secret-keys $$GPG_SIGNING_KEY_ID)" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyId="$$GPG_SIGNING_KEY_ID" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=$$(security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w)

default: versioncheck

help:  ## Show this help (list targets with descriptions)
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  \033[36m%-22s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build-all: clean stage  ## Clean and run a full Heroku stage build

stop:  ## Stop the Gradle daemon
	./gradlew --stop

clean:  ## Remove Gradle build artifacts
	./gradlew clean

build: clean  ## Clean and build (skips tests)
	./gradlew build -xtest

local-build: clean  ## Clean and build using Maven Local artifacts
	./gradlew build -PuseMavenLocal=true -xtest

lint: detekt  ## Run Detekt and Kotlinter lint on main and test sources
	./gradlew lintKotlinMain lintKotlinTest

detekt:  ## Run Detekt static analysis
	./gradlew detekt

refresh:  ## Refresh dependencies and build (skips tests)
	./gradlew --refresh-dependencies build -xtest

tests:  ## Clean test results and re-run the test suite
	./gradlew cleanTest test

fatjar:  ## Build the executable fat JAR for kslides-examples
	./gradlew buildFatJar

uber: fatjar  ## Build the fat JAR and run the example presentation
	java -jar kslides-examples/build/libs/kslides.jar

dist:  ## Assemble the distribution via installDist
	./gradlew installDist

stage:  ## Heroku deployment build
	./gradlew stage

deps:  ## Print the Gradle dependency tree
	./gradlew -q dependencies

process-resources:  ## Run kslides-core processResources (grafts reveal.js assets)
	./gradlew :kslides-core:processResources

versioncheck:  ## Check for dependency updates (default target)
	./gradlew dependencyUpdates --no-configuration-cache --no-parallel

kdocs:  ## Generate Dokka HTML API docs
	./gradlew :dokkaGenerate

clean-docs:  ## Remove generated docs and Zensical site artifacts
	rm -rf docs/playground docs/letsPlot docs/kroki
	rm -rf website/kslides/site website/kslides/.cache

site: clean-docs  ## Serve the Zensical docs site locally
	cd website/kslides && uv run zensical serve

publish-local: _require-version ## Publish artifacts to Maven Local
	./gradlew publishToMavenLocal

publish-local-snapshot: _require-version ## Publish a -SNAPSHOT build to Maven Local
	./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenLocal

# publish-snapshot stages a -SNAPSHOT build to the Sonatype snapshots repo (vanniktech routes
# -SNAPSHOT versions there automatically). publish-maven-central uses the *AndRelease* variant
# to both stage and auto-release the version on the Central Portal — do not "fix" the asymmetry.
publish-snapshot: _require-version _check-gpg-env ## Stage a signed -SNAPSHOT to Sonatype snapshots
	$(GPG_ENV) ./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenCentral

publish-maven-central: _require-version _check-gpg-env ## Stage and release a signed build to Maven Central
	$(GPG_ENV) ./gradlew publishAndReleaseToMavenCentral

# Gradle's documented upgrade procedure: the first run rewrites
# gradle-wrapper.properties using the *old* wrapper jar; the second run
# regenerates the wrapper itself with the new version.
upgrade-wrapper: _require-gradle-version  ## Upgrade the Gradle wrapper to the version pinned in libs.versions.toml
	./gradlew wrapper --gradle-version=$(GRADLE_VERSION) --distribution-type=bin
	./gradlew wrapper --gradle-version=$(GRADLE_VERSION) --distribution-type=bin

_check-gpg-env:
	@if [ -z "$$GPG_SIGNING_KEY_ID" ]; then \
		echo "ERROR: GPG_SIGNING_KEY_ID is not set" >&2; exit 1; \
	fi
	@if ! gpg --list-secret-keys "$$GPG_SIGNING_KEY_ID" >/dev/null 2>&1; then \
		echo "ERROR: no GPG secret key found for GPG_SIGNING_KEY_ID=$$GPG_SIGNING_KEY_ID" >&2; exit 1; \
	fi
	@if ! security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w >/dev/null 2>&1; then \
		echo "ERROR: keychain entry 'gradle-signing-password' (account 'gpg-signing') not found" >&2; exit 1; \
	fi

_require-version:
	@[ -n "$(VERSION)" ] || { echo "ERROR: Could not determine project version from gradle.properties" >&2; exit 1; }

_require-gradle-version:
	@[ -n "$(GRADLE_VERSION)" ] || { echo "ERROR: Could not determine gradle version from gradle/libs.versions.toml" >&2; exit 1; }
