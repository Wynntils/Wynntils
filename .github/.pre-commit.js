const fs = require("fs");

exports.preCommit = (props) => {
    const replace = (path, searchValue, replaceValue) => {
        let content = fs.readFileSync(path, "utf-8");
        if (content.match(searchValue)) {
            fs.writeFileSync(path, content.replace(searchValue, replaceValue));
            console.log(`"${path}" changed`);
        }
    };

    // replace only the version string example:
    // version = "0.0.3-alpha.2"
    // version = "0.0.3-alpha.103+MC-1.19.4"
    replace("./build.gradle", /(?<=version = ")\d+\.\d+\.\d+(-\w+\.\d+)?(\+MC-\d\.\d+\.\d+)?(-SNAPSHOT)?(?=")/g, props.version);
    // Regex provided by Github Copilot
};
