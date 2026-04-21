default: versioncheck

build-all: clean stage

stop:
	./gradlew --stop

clean:
	./gradlew clean

build: clean
	./gradlew build -xtest

refresh:
	./gradlew --refresh-dependencies

tests:
	./gradlew --rerun-tasks check

uberjar:
	./gradlew uberjar

uber: uberjar
	java -jar build/libs/kslides.jar

dist:
	./gradlew installDist

stage:
	./gradlew stage

dokka:
	./gradlew dokkaHtml

clean-docs:
	rm -rf docs/playground docs/letplot docs/kroki

tree:
	./gradlew -q dependencies

versioncheck:
	./gradlew dependencyUpdates --no-configuration-cache

upgrade-wrapper:
	./gradlew wrapper --gradle-version=9.4.1 --distribution-type=bin
