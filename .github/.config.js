"use strict";
const config = require("conventional-changelog-conventionalcommits");

module.exports = config({
    types: [
        // Unhide all types except "ci" so that they show up on generated changelog
        // Default values:
        // https://github.com/conventional-changelog/conventional-changelog/blob/master/packages/conventional-changelog-conventionalcommits/writer-opts.js
        { type: "feat", section: "New Features" },
        { type: "feature", section: "New Features" },
        { type: "fix", section: "Bug Fixes" },
        { type: "perf", section: "Performance Improvements" },
        { type: "revert", section: "Reverts" },
        { type: "docs", section: "Documentation" },
        { type: "style", section: "Styles" },
        { type: "chore", section: "Miscellaneous Chores" },
        { type: "refactor", section: "Code Refactoring" },
        { type: "test", section: "Tests" },
        { type: "build", section: "Build System" },
        { type: "ci", section: "Continuous Integration", hidden: true },
    ],
});
