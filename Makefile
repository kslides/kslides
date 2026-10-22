.PHONY: default help build-all stop clean clean-cache slides clean-slides build local-build lint detekt refresh tests \
        fatjar uber dist stage deps process-resources versions versions-all dev-server kroki-start kroki-stop \
        pdf clean-pdf kdocs zizmor check-site upgrade-site clean-site site \
        publish-local publish-local-snapshot publish-snapshot publish-maven-central upgrade-wrapper \
        _check-gpg-env _require-kroki _require-version _require-gradle-version

VERSION := $(shell sed -n 's/^version=\(.*\)/\1/p' gradle.properties)
GRADLE_VERSION := $(shell sed -n 's/^gradle-wrapper = "\(.*\)"/\1/p' gradle/libs.versions.toml)
GRADLE_SHA256_URL = https://services.gradle.org/distributions/gradle-$(GRADLE_VERSION)-bin.zip.sha256

WEBSITE_DIR := website
SITE_DIR    := $(WEBSITE_DIR)/kslides
KROKI_DIR   := kroki

GPG_ENV = \
	ORG_GRADLE_PROJECT_signingInMemoryKey="$$(gpg --armor --export-secret-keys $$GPG_SIGNING_KEY_ID)" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyId="$$GPG_SIGNING_KEY_ID" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=$$(security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w)

default: help

help:  ## Show this help (list of targets)
	@awk 'BEGIN {FS = ":.*?## "; printf "Usage: make <target>\n\nTargets:\n"} \
		/^[a-zA-Z0-9_-]+:.*?## / {printf "  \033[36m%-24s\033[0m %s\n", $$1, $$2}' \
		$(MAKEFILE_LIST)

build-all: clean stage  ## Clean and run a full Heroku stage build

stop:  ## Stop the Gradle daemon
	./gradlew --stop

clean:  ## Remove Gradle build artifacts
	./gradlew clean

clean-cache: clean  ## Clean and delete the Gradle configuration cache
	rm -rf .gradle/configuration-cache

# clean-slides runs in the recipe rather than as a prerequisite so it can only follow the Kroki
# check, even under make -j: a missing server fails the target before anything is deleted.
slides: _require-kroki  ## Regenerate the example slides under docs/ (needs kroki-start)
	@$(MAKE) --no-print-directory clean-slides
	./gradlew :kslides-examples:writeDocs

clean-slides:  ## Remove the generated example slides under docs/
	rm -rf docs/demo1 docs/kroki docs/letsPlot docs/playground docs/*.html

build: clean  ## Clean and build (skips tests)
	./gradlew build -x test

local-build: clean  ## Clean and build using Maven Local artifacts
	./gradlew build -PuseMavenLocal=true -x test

lint: detekt  ## Run Detekt and Kotlinter lint on main and test sources
	./gradlew lintKotlinMain lintKotlinTest

detekt:  ## Run Detekt static analysis
	./gradlew detekt

refresh:  ## Refresh dependencies and build (skips tests)
	./gradlew --refresh-dependencies build -x test

tests:  ## Clean test results and re-run the test suite
	./gradlew cleanTest test --no-build-cache

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

# dependencyUpdates is marked incompatible with the configuration cache (kslides.stable-versions), so a run with the
# cache on builds an entry only to discard it, and says so. --no-configuration-cache skips the cache for this run
# instead. Only the root task runs: it merges every module's report, where the unqualified name would also print each
# module's own. kslides.stable-versions gives the task a formatter that lists only what is behind; versions-all names
# the plugin's own plain report, which also lists everything up to date.
versions:  ## List the dependencies with newer versions, and any that could not be checked
	./gradlew :dependencyUpdates --no-configuration-cache --no-parallel

versions-all:  ## The plugin's full dependency report, up-to-date dependencies included
	./gradlew :dependencyUpdates --no-configuration-cache --no-parallel --output-formatter=plain

dev-server:  ## Run the live-reload dev loop (watch sources, rebuild, reload the browser)
	./kslides-dev.sh --task :kslides-examples:run --watch kslides-examples/src

kroki-start:  ## Start a local Kroki diagram server on port 8000 (docker-compose)
	cd $(KROKI_DIR) && docker-compose up -d

kroki-stop:  ## Stop the local Kroki diagram server
	cd $(KROKI_DIR) && docker-compose down

# The example deck's Kroki diagrams render through the local Kroki server (make kroki-start).
pdf:  ## Export the example presentations to PDF in build/pdf (optionally DECK=<name>)
	./gradlew :kslides-examples:exportPdf $(if $(DECK),-Pdeck=$(DECK))

clean-pdf:  ## Remove the exported PDFs
	rm -rf build/pdf

kdocs:  ## Generate Dokka HTML API docs
	./gradlew :dokkaGenerate

zizmor:  ## Audit the GitHub Actions workflows with zizmor
	@command -v zizmor >/dev/null 2>&1 || { echo "ERROR: zizmor not found (brew install zizmor, or uv tool install zizmor)" >&2; exit 1; }
	zizmor .

check-site:  ## Check for outdated website dependencies
	cd $(WEBSITE_DIR) && env -u VIRTUAL_ENV uv lock --upgrade --dry-run

upgrade-site:  ## Upgrade the website dependencies
	cd $(WEBSITE_DIR) && env -u VIRTUAL_ENV uv lock --upgrade

clean-site:  ## Remove generated docs and Zensical site artifacts
	rm -rf docs/playground docs/letsPlot docs/kroki
	rm -rf $(SITE_DIR)/site $(SITE_DIR)/.cache

site: clean-site  ## Serve the Zensical docs site locally
	cd $(SITE_DIR) && env -u VIRTUAL_ENV uv run zensical serve

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
#
# Both runs pass the distribution's published SHA-256, which the wrapper records
# as distributionSha256Sum and checks every download against. Once it is pinned,
# Gradle refuses to change the version without a new one. One shell line, so
# both runs see the same checksum.
upgrade-wrapper: _require-gradle-version  ## Upgrade the Gradle wrapper to the version pinned in libs.versions.toml
	@sha=$$(curl -fsSL --max-time 30 $(GRADLE_SHA256_URL)) && echo "$$sha" | grep -Eq '^[0-9a-f]{64}$$' \
		|| { echo "ERROR: could not fetch the SHA-256 for Gradle $(GRADLE_VERSION) from $(GRADLE_SHA256_URL)" >&2; exit 1; }; \
	for run in first second; do \
		echo "./gradlew wrapper --gradle-version=$(GRADLE_VERSION) --distribution-type=bin --gradle-distribution-sha256-sum=$$sha"; \
		./gradlew wrapper --gradle-version=$(GRADLE_VERSION) --distribution-type=bin --gradle-distribution-sha256-sum=$$sha || exit 1; \
	done

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

_require-kroki:
	@curl -s -o /dev/null --max-time 5 http://localhost:8000/ || { echo "ERROR: no Kroki server on localhost:8000 (make kroki-start)" >&2; exit 1; }

_require-version:
	@[ -n "$(VERSION)" ] || { echo "ERROR: Could not determine project version from gradle.properties" >&2; exit 1; }

_require-gradle-version:
	@[ -n "$(GRADLE_VERSION)" ] || { echo "ERROR: Could not determine gradle version from gradle/libs.versions.toml" >&2; exit 1; }
