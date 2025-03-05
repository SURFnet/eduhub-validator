# Recipes for building release artifacts
#
# Expects the artifact to be in the format
#
#        `eduhub-validator-VERSION-ARCH.EXT
#
# Where VERSION matches the current git tag (like `v0.0.1-SNAPSHOT`)
#
# And ARCH.EXT is some reasonable combination, like
# `linux-amd64.tar.gz`, `standalone.jar` or `windows-amd64.zip`
#
# Standalone binaries are supported for every platform supported by
# babashka (variants of linux, windows and macos), see
# https://github.com/babashka/babashka/releases
#
# In other words, `make eduhub-validator-$VERSION-windows-amd64.zip`
# and `make eduhub-validator-$VERSION-macos-aarch64.tar.gz` will do
# what you expect as long as $VERSION is the currently tagged version

BB:=bb
version:=$(shell git describe --tags)

# need latest snapshot for standalone executables
BABASHKA_VERSION:=1.3.188

.PHONY: uberjar

exec_base_name=eduhub-validator
release_name=$(exec_base_name)-$(version)
source_files=$(shell find profiles src -type f)
source_files+=generated/resources/extra.css
current_arch=$(shell bb current_arch.clj)

# uberjar is the babashka uberjar (not a java-compatible jar)
uberjar=$(exec_base_name)-$(version)-standalone.jar

uberjar: $(uberjar)

$(uberjar): deps.edn bb.edn $(source_files) bake-version
	rm -f $@
	$(BB) uberjar $@ -m nl.surf.eduhub-validator.main

bake-version:
	if [ "$(version)" != "$(baked_version)" ]; then	echo "$(version)" >src/nl/surf/eduhub_validator/version.txt; fi

release: $(binary_release)

# for unixy systems

$(release_name)-%/$(exec_base_name): babashka-$(BABASHKA_VERSION)-%.tar.gz $(uberjar)
	mkdir -p $(dir $@)
	tar -zxO <$< >$@
	cat $(uberjar) >>$@
	chmod 755 $@

# for windows
$(release_name)-%/$(exec_base_name).exe: babashka-$(BABASHKA_VERSION)-%.zip $(uberjar)
	mkdir -p $(dir $@)
	unzip -p $< >$@
	cat $(uberjar) >>$@

babashka-$(BABASHKA_VERSION)-%:
	curl -sL https://github.com/babashka/babashka-dev-builds/releases/download/v$(BABASHKA_VERSION)/$@ -o $@

# for unixy systems
$(release_name)-%.tar.gz: $(release_name)-%/$(exec_base_name)
	tar -rf tmp.tar $<
	gzip <tmp.tar >$@
	rm tmp.tar

# for windows
$(release_name)-%.zip: $(release_name)-%/$(exec_base_name).exe
	zip -r $@ $(basename $@)

# build for local use, on windows
$(exec_base_name).exe: $(release_name)-$(current_arch)/$(exec_base_name).exe
	cp $< $@

# build for local use, non-windows
$(exec_base_name): $(uberjar)
	cat $(shell which bb) $(uberjar) >$@
	chmod 755 $@

usage.txt.generated: $(exec_base_name)
	echo "\`\`\`" >$@
	"./$(exec_base_name)" --help |sed -n '/^Usage:/,/\Z/p' >>$@
	echo "\`\`\`" >>$@

README.md: usage.txt.generated README.src.md
	echo "<!-- WARNING! THIS FILE IS GENERATED, EDIT README.src.md INSTEAD -->" >$@
	sed "/<!-- INCLUDE USAGE HERE -->/r $<" README.src.md >>$@

# check that working tree is clean when generated files are up-to-date
#
# we check in the generated css file, so that it's available when the
# validator is used as a library dependency.
release_check: README.md generated/resources/extra.css
	exit $$(git status --porcelain | wc -l)

# Note the order of CSS files is important
sds_green_css_files=$(shell find assets/sds-green -name \*.css | sort)

generated/resources/extra.css: $(sds_green_css_files)
	mkdir -p generated/resources
	cat $(sds_green_css_files) > $@
