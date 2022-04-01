default: versioncheck

build-all: clean stage

clean:
	./gradlew clean

compile: build

build:
	./gradlew build -xtest

uberjar:
	./gradlew uberjar

uber: uberjar
	java -jar build/libs/kslides.jar

dist:
	./gradlew installDist

stage:
	./gradlew stage

versioncheck:
	./gradlew dependencyUpdates

upgrade-wrapper:
	./gradlew wrapper --gradle-version=7.4.2 --distribution-type=bin