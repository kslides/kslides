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

dokka:
	./gradlew dokkaHtml

cleandocs:
	rm -rf docs/playground docs/plotly

versioncheck:
	./gradlew dependencyUpdates

upgrade-wrapper:
	./gradlew wrapper --gradle-version=7.5 --distribution-type=bin