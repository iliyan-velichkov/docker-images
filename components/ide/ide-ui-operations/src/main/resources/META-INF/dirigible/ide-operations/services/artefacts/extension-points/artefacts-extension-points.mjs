import { query } from "@dirigible/db";
import { Utils } from "../Utils.mjs";

export const getArtefacts = () => {
    const sql = `
        SELECT 
        ARTEFACT_TYPE, ARTEFACT_LOCATION, ARTEFACT_NAME, ARTEFACT_PHASE, ARTEFACT_RUNNING, ARTEFACT_STATUS
        FROM DIRIGIBLE_EXTENSION_POINTS
    `;
    const resultset = query.execute(sql, [], "SystemDB");
    return resultset.map(e => Utils.getArtefactStatus(e));
};