import express from "express";
import cors from "cors";

const app = express();
const PORT = process.env.PORT || 10000;
const API_KEY = process.env.NEIS_API_KEY;
const OFFICE_CODE = "J10";
const SCHOOL_NAME = "군포중앙고등학교";
const NEIS = "https://open.neis.go.kr/hub";

app.use(cors({ origin: process.env.FRONTEND_ORIGIN || true }));
app.get("/health", (_req, res) => res.json({ ok: true }));

function required(value, name) {
  if (!value) {
    const error = new Error(`${name}이(가) 필요합니다.`);
    error.status = 400;
    throw error;
  }
  return value;
}

async function neis(path, params) {
  if (!API_KEY) {
    const error = new Error("서버에 NEIS_API_KEY가 설정되지 않았습니다.");
    error.status = 500;
    throw error;
  }
  const url = new URL(`${NEIS}/${path}`);
  url.search = new URLSearchParams({ KEY: API_KEY, Type: "json", pIndex: "1", pSize: "100", ...params });
  const response = await fetch(url);
  if (!response.ok) throw new Error(`NEIS HTTP ${response.status}`);
  return response.json();
}

app.get("/api/school", async (_req, res) => {
  try {
    const data = await neis("schoolInfo", { ATPT_OFCDC_SC_CODE: OFFICE_CODE, SCHUL_NM: SCHOOL_NAME });
    const rows = data?.schoolInfo?.[1]?.row || [];
    const school = rows.find((row) => row.SCHUL_NM === SCHOOL_NAME) || rows[0];
    if (!school?.SD_SCHUL_CODE) return res.status(404).json({ error: "학교 코드를 찾지 못했습니다." });
    res.json({ officeCode: OFFICE_CODE, schoolCode: school.SD_SCHUL_CODE, schoolName: school.SCHUL_NM });
  } catch (error) {
    res.status(error.status || 502).json({ error: error.message });
  }
});

app.get("/api/timetable", async (req, res) => {
  try {
    const date = required(req.query.date, "date");
    const grade = required(req.query.grade, "grade");
    const classNo = required(req.query.classNo, "classNo");
    if (!/^\d{8}$/.test(date)) return res.status(400).json({ error: "date 형식이 올바르지 않습니다." });
    const schoolData = await neis("schoolInfo", { ATPT_OFCDC_SC_CODE: OFFICE_CODE, SCHUL_NM: SCHOOL_NAME });
    const rows = schoolData?.schoolInfo?.[1]?.row || [];
    const school = rows.find((row) => row.SCHUL_NM === SCHOOL_NAME) || rows[0];
    if (!school?.SD_SCHUL_CODE) return res.status(404).json({ error: "학교 코드를 찾지 못했습니다." });
    const data = await neis("hisTimetable", {
      ATPT_OFCDC_SC_CODE: OFFICE_CODE,
      SD_SCHUL_CODE: school.SD_SCHUL_CODE,
      TI_FROM_YMD: date,
      TI_TO_YMD: date,
      GRADE: grade,
      CLASS_NM: classNo
    });
    res.json({ rows: data?.hisTimetable?.[1]?.row || [] });
  } catch (error) {
    res.status(error.status || 502).json({ error: error.message });
  }
});

app.listen(PORT, "0.0.0.0", () => console.log(`API listening on ${PORT}`));
