/*
 * Generated by Eclipse Dirigible based on model and template.
 *
 * Do not modify the content as it may be re-generated again.
 */
const generateUtils = dirigibleRequire("ide-generate-service/template/generateUtils");
const parameterUtils = dirigibleRequire("ide-generate-service/template/parameterUtils");

exports.generate = function (model, parameters) {
    model = JSON.parse(model).model;
    let templateSources = exports.getTemplate(parameters).sources;
    parameterUtils.process(model, parameters)
    return generateUtils.generateFiles(model, parameters, templateSources);
};

exports.getTemplate = function (parameters) {
    return {
        name: "Application - Schema",
        description: "Application with a Database Schema",
        extension: "model",
        sources: [{
            location: "/template-application-schema/data/application.schema.template",
            action: "generate",
            rename: "gen/schema/{{projectName}}.schema",
            engine: "velocity"
        }],
        parameters: [{
            name: "tablePrefix",
            label: "Table Prefix",
            placeholder: "Table prefix"
        }]
    };
};