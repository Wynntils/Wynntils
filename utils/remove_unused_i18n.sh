#!/bin/bash

langs_dir="common/src/main/resources/assets/wynntils/lang"
en_us="$langs_dir/en_us.json"

# Get the staged version of en_us.json
staged_en_us=$(git show :"$en_us")

# Create a temporary file for the keys (stored as a JSON array)
keys_file=$(mktemp)
echo "$staged_en_us" | jq -c 'keys' > "$keys_file"

# Iterate over each language file in the langs_dir
shopt -s nullglob  # Prevent literal "*.json" expansion if no files exist
for file in "$langs_dir"/*.json; do
    # Skip the en_us.json file itself
    if [[ "$file" == "$en_us" ]]; then
        continue
    fi

    # Verify file exists and is a regular file
    if [[ ! -f "$file" ]]; then
        echo "Warning: $file is not a valid file. Skipping..." >&2
        continue
    fi

    # Get the staged version of the translation file
    staged_file=$(git show :"$file")

    # Get the HEAD version of en_us.json and the translation file
    head_en_us=$(git show HEAD:"$en_us" 2>/dev/null || echo "{}")
    head_file=$(git show HEAD:"$file" 2>/dev/null || echo "{}")

    # Create temporary files to store the JSON data
    staged_en_us_file=$(mktemp)
    staged_file_file=$(mktemp)
    head_en_us_file=$(mktemp)
    head_file_file=$(mktemp)

    echo "$staged_en_us" > "$staged_en_us_file"
    echo "$staged_file" > "$staged_file_file"
    echo "$head_en_us" > "$head_en_us_file"
    echo "$head_file" > "$head_file_file"

    # Create a temporary file to store the filtered content
    temp_file=$(mktemp)

    # Process keys using jq
    echo "$staged_file" | jq --slurpfile keys "$keys_file" \
        --slurpfile staged_en_us "$staged_en_us_file" \
        --slurpfile head_en_us "$head_en_us_file" \
        --slurpfile head_file "$head_file_file" \
        'with_entries(select(
            .key as $k | ($keys[0] | index($k)) and (
                ($staged_en_us[0][$k] == $head_en_us[0][$k]) or
                ($staged_en_us[0][$k] != $head_en_us[0][$k] and .[$k] != $head_file[0][$k])
            )
        ))' > "$temp_file"

    # Replace the original file with the filtered content
    mv "$temp_file" "$file"

    # Clean up temporary files
    rm -f "$staged_en_us_file" "$staged_file_file" "$head_en_us_file" "$head_file_file"
done

# Clean up the temporary keys file
rm -f "$keys_file"