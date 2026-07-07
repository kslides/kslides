#!/usr/bin/env bash
#
# kslides-init.sh — scaffold a new kslides presentation project from the
# kslides-template repo (https://github.com/kslides/kslides-template).
#
# Usage:
#   kslides-init.sh <project-name> [--title "My Talk"]
#
# The script clones the template, strips its git history, renames the project
# and presentation title, and initializes a fresh git repository.
#
# Portability: runs on macOS's default bash 3.2 and on modern Linux bash.

set -euo pipefail

TEMPLATE_REPO="${KSLIDES_TEMPLATE_REPO:-https://github.com/kslides/kslides-template}"
SCRIPT_NAME="$(basename "$0")"
# When piped to a shell (curl ... | bash), $0 is the shell name, not the script.
case "$SCRIPT_NAME" in
  bash | -bash | sh | -sh | dash | zsh | -zsh) SCRIPT_NAME="kslides-init.sh" ;;
esac

usage() {
  cat <<EOF
Usage: ${SCRIPT_NAME} <project-name> [--title "My Talk"]

Scaffold a new kslides presentation project from
${TEMPLATE_REPO}.

Arguments:
  <project-name>   Directory and Gradle project name to create.
                   Letters, digits, dashes, and underscores only;
                   must start with a letter or digit.

Options:
  --title TITLE    Presentation title (defaults to the project name).
  -h, --help       Show this help.

Environment:
  KSLIDES_TEMPLATE_REPO   Override the template repository URL.
EOF
}

die() {
  printf 'Error: %s\n' "$1" >&2
  exit 1
}

# Escape a string for use in the replacement part of a sed "s|…|…|" expression.
escape_sed_replacement() {
  printf '%s' "$1" | sed -e 's/[\\&|]/\\&/g'
}

# Portable in-place sed: BSD sed (macOS) and GNU sed (Linux) disagree on the
# syntax of -i, so write to a temp file and move it into place instead.
sed_inplace() {
  local sed_expr="$1" file="$2"
  local tmp="${file}.kslides-init.tmp"
  sed -e "$sed_expr" "$file" >"$tmp"
  mv "$tmp" "$file"
}

# rename_in_file <sed-expression> <literal-match> <file>
# Applies the sed expression only if the literal match is present; warns
# (without failing) if the template has drifted and the text is gone.
rename_in_file() {
  local sed_expr="$1" match="$2" file="$3"
  if grep -qF -- "$match" "$file"; then
    sed_inplace "$sed_expr" "$file"
  else
    printf 'Warning: expected "%s" in %s; the template may have changed. Skipping this rename.\n' \
      "$match" "$file" >&2
  fi
}

project_dir=""
created_dir=0

cleanup() {
  local status=$?
  if [ "$status" -ne 0 ] && [ "$created_dir" -eq 1 ] && [ -n "$project_dir" ] && [ -e "$project_dir" ]; then
    rm -rf "$project_dir"
    printf 'Cleaned up partially created directory: %s\n' "$project_dir" >&2
  fi
}
trap cleanup EXIT

# --- Parse arguments --------------------------------------------------------

project_name=""
title=""

while [ $# -gt 0 ]; do
  case "$1" in
    --title)
      [ $# -ge 2 ] || die "--title requires a value"
      title="$2"
      shift 2
      ;;
    --title=*)
      title="${1#--title=}"
      shift
      ;;
    -h | --help)
      usage
      exit 0
      ;;
    -*)
      printf 'Error: unknown option: %s\n\n' "$1" >&2
      usage >&2
      exit 1
      ;;
    *)
      if [ -z "$project_name" ]; then
        project_name="$1"
      else
        die "unexpected extra argument: $1"
      fi
      shift
      ;;
  esac
done

if [ -z "$project_name" ]; then
  if [ -t 0 ]; then
    printf 'Project name (letters, digits, dashes, underscores): '
    read -r project_name
    if [ -z "$title" ]; then
      printf 'Presentation title [%s]: ' "$project_name"
      read -r title
    fi
  else
    usage >&2
    exit 1
  fi
fi

# --- Validate ---------------------------------------------------------------

name_re='^[A-Za-z0-9][A-Za-z0-9_-]*$'
[[ "$project_name" =~ $name_re ]] ||
  die "invalid project name '$project_name' (use letters, digits, dashes, underscores; must start with a letter or digit)"

[ -n "$title" ] || title="$project_name"
case "$title" in
  *$'\n'*) die "title must not contain newlines" ;;
esac

command -v git >/dev/null 2>&1 || die "git is required but was not found on PATH"

project_dir="$project_name"
[ ! -e "$project_dir" ] || die "'$project_dir' already exists; refusing to overwrite it"

# --- Clone the template -----------------------------------------------------

printf 'Cloning %s into %s/ ...\n' "$TEMPLATE_REPO" "$project_dir"
created_dir=1
git clone --depth 1 --quiet "$TEMPLATE_REPO" "$project_dir"
rm -rf "$project_dir/.git"

# --- Apply renames ----------------------------------------------------------

printf 'Renaming project to "%s" (title: "%s") ...\n' "$project_name" "$title"
title_sed="$(escape_sed_replacement "$title")"

rename_in_file \
  "s|rootProject\.name = \"kslides-template\"|rootProject.name = \"${project_name}\"|" \
  'rootProject.name = "kslides-template"' \
  "$project_dir/settings.gradle.kts"

rename_in_file \
  "s|^# kslides Template\$|# ${title_sed}|" \
  '# kslides Template' \
  "$project_dir/README.md"

rename_in_file \
  "s|^A template repo for authoring |A |" \
  'A template repo for authoring ' \
  "$project_dir/README.md"

rename_in_file \
  "s|repo = \"kslides-template\"|repo = \"${project_name}\"|" \
  'repo = "kslides-template"' \
  "$project_dir/src/main/kotlin/Slides.kt"

rename_in_file \
  "s|# Markdown Slide\$|# ${title_sed}|" \
  '# Markdown Slide' \
  "$project_dir/src/main/kotlin/Slides.kt"

rename_in_file \
  "s|\"name\": \"kslides Template\"|\"name\": \"${project_name}\"|" \
  '"name": "kslides Template"' \
  "$project_dir/app.json"

rename_in_file \
  "s|\"description\": \"A template for getting started with kslides.\"|\"description\": \"A kslides presentation.\"|" \
  '"description": "A template for getting started with kslides."' \
  "$project_dir/app.json"

rename_in_file \
  "s|^# kslides-template\$|# ${project_name}|" \
  '# kslides-template' \
  "$project_dir/llms.txt"

# Template release history belongs to kslides-template, not to the new deck.
rm -f "$project_dir/CHANGELOG.md" "$project_dir/RELEASE_NOTES.md"

# --- Initialize a fresh repository ------------------------------------------

printf 'Initializing a fresh git repository ...\n'
git -C "$project_dir" init --quiet

# --- Done -------------------------------------------------------------------

cat <<EOF

Created ${project_dir}/ from ${TEMPLATE_REPO}.

Next steps:
  cd ${project_dir}
  ./gradlew build -x test   # first build (downloads dependencies)
  ./gradlew run             # regenerate docs/ and serve http://localhost:8080
  make help                 # list build shortcuts (make build, make uber, ...)

Then:
  - Edit src/main/kotlin/Slides.kt to author your slides.
  - Update 'group' in gradle.properties, and point the GitHub links in
    Slides.kt (topLeftHref and the srcrefLink account/repo) at your own repo.
  - Rerun the deck (./gradlew run, or the green arrow on 'fun main()' in
    IntelliJ) to regenerate the static HTML in docs/ before publishing to
    GitHub Pages or Netlify — see README.md for the deployment guides.
  - git add -A && git commit -m "Initial commit" when you are ready.
EOF
