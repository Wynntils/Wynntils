const fs = require("fs");

exports.preCommit = (props) => {
    const replace = (path, searchValue, replaceValue) => {
        let content = fs.readFileSync(path, "utf-8");
        if (content.match(searchValue)) {
            fs.writeFileSync(path, content.replace(searchValue, replaceValue));
            console.log(`"${path}" changed`);
        }
    };

    const [major, minor, patch] = props.version.split(".");

    replace("./build.gradle", /(?<=major: )\d+/g, major);
    replace("./build.gradle", /(?<=minor: )\d+/g, minor);
    replace("./build.gradle", /(?<=patch: )\d+/g, patch);
};
