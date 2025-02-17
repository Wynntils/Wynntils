#!/bin/bash

langs_dir="common/src/main/resources/assets/wynntils/lang"
en_us="$langs_dir/en_us.json"

# Get the staged version of en_us.json
staged_en_us=$(git show :"$en_us")

# Create a temporary file for the keys (stored as a JSON array)
keys_file=$(mktemp)
echo "$staged_en_us" | jq -c 'keys' > "$keys_file"

# Iterate over each language file in the langs_dir
for file in "$langs_dir"/*.json; do
    # Skip the en_us.json file itself
    if [[ "$file" == "$en_us" ]]; then
        continue
    fi

    # Get the staged version of the translation file
    staged_file=$(git show :"$file")

    # Get the HEAD version of en_us.json and the translation file
    head_en_us=$(git show HEAD:"$en_us" 2>/dev/null || echo "{}")
    head_file=$(git show HEAD:"$file" 2>/dev/null || echo "{}")

    # Create a temporary file to store the filtered content
    temp_file=$(mktemp)

    # Process each key using the temporary file
    # Keep the key if:
    # 1. The value in en_us.json has NOT changed, OR
    # 2. The value in en_us.json has changed, AND the translation file value has also changed
    echo "$staged_file" | jq --slurpfile keys "$keys_file" \
        --argjson staged_en_us "$staged_en_us" \
        --argjson head_en_us "$head_en_us" \
        --argjson head_file "$head_file" \
        'with_entries(select(
            .key as $k | ($keys[0] | index($k)) and (
                ($staged_en_us[$k] == $head_en_us[$k]) or
                ($staged_en_us[$k] != $head_en_us[$k] and .[$k] != $head_file[$k])
            )
    ))' > "$temp_file"

    # Replace the original file with the filtered content
    mv "$temp_file" "$file"
done

# Clean up the temporary keys file
rm -f "$keys_file"