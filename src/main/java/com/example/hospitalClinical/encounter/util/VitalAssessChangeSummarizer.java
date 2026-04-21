package com.example.hospitalClinical.encounter.util;

import com.example.hospitalClinical.encounter.entity.ClinicalVitalAssess;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class VitalAssessChangeSummarizer {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_LEN = 3900;

    private VitalAssessChangeSummarizer() {}

    public record Snapshot(
            LocalDateTime recordedAt,
            Long receptionId,
            Integer systolicBp,
            Integer diastolicBp,
            Integer pulse,
            Integer respiration,
            BigDecimal temperature,
            Integer spo2,
            Integer painScore,
            String consciousnessLevel,
            String heightCm,
            String weightKg,
            String chiefComplaint,
            String visitReason,
            String historyPresentIllness,
            String pastHistory,
            String familyHistory,
            String allergy,
            String currentMedication,
            String status) {

        public static Snapshot from(ClinicalVitalAssess e) {
            return new Snapshot(
                    e.getRecordedAt(),
                    e.getReceptionId(),
                    e.getSystolicBp(),
                    e.getDiastolicBp(),
                    e.getPulse(),
                    e.getRespiration(),
                    e.getTemperature(),
                    e.getSpo2(),
                    e.getPainScore(),
                    e.getConsciousnessLevel(),
                    e.getHeightCm(),
                    e.getWeightKg(),
                    e.getChiefComplaint(),
                    e.getVisitReason(),
                    e.getHistoryPresentIllness(),
                    e.getPastHistory(),
                    e.getFamilyHistory(),
                    e.getAllergy(),
                    e.getCurrentMedication(),
                    e.getStatus());
        }
    }

    public static String summarize(Snapshot b, ClinicalVitalAssess a) {
        if (a == null) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        if (!eqLdT(b.recordedAt(), a.getRecordedAt())) {
            lines.add("측정시각: 「" + fmtDateTime(b.recordedAt()) + "」→「" + fmtDateTime(a.getRecordedAt()) + "」");
        }
        if (!eqLong(b.receptionId(), a.getReceptionId())) {
            lines.add("접수번호: 「" + fmtLong(b.receptionId()) + "」→「" + fmtLong(a.getReceptionId()) + "」");
        }
        if (!eqInt(b.systolicBp(), a.getSystolicBp())) {
            lines.add("수축기혈압: 「" + fmtInt(b.systolicBp()) + "」→「" + fmtInt(a.getSystolicBp()) + "」");
        }
        if (!eqInt(b.diastolicBp(), a.getDiastolicBp())) {
            lines.add("이완기혈압: 「" + fmtInt(b.diastolicBp()) + "」→「" + fmtInt(a.getDiastolicBp()) + "」");
        }
        if (!eqInt(b.pulse(), a.getPulse())) {
            lines.add("맥박: 「" + fmtInt(b.pulse()) + "」→「" + fmtInt(a.getPulse()) + "」");
        }
        if (!eqInt(b.respiration(), a.getRespiration())) {
            lines.add("호흡: 「" + fmtInt(b.respiration()) + "」→「" + fmtInt(a.getRespiration()) + "」");
        }
        if (!eqBd(b.temperature(), a.getTemperature())) {
            lines.add("체온: 「" + fmtBd(b.temperature()) + "」→「" + fmtBd(a.getTemperature()) + "」");
        }
        if (!eqInt(b.spo2(), a.getSpo2())) {
            lines.add("산소포화도: 「" + fmtInt(b.spo2()) + "」→「" + fmtInt(a.getSpo2()) + "」");
        }
        if (!eqInt(b.painScore(), a.getPainScore())) {
            lines.add("통증: 「" + fmtInt(b.painScore()) + "」→「" + fmtInt(a.getPainScore()) + "」");
        }
        if (!eqStr(b.consciousnessLevel(), a.getConsciousnessLevel())) {
            lines.add("의식: 「" + fmtStr(b.consciousnessLevel()) + "」→「" + fmtStr(a.getConsciousnessLevel()) + "」");
        }
        if (!eqStr(b.heightCm(), a.getHeightCm())) {
            lines.add("키(cm): 「" + fmtStr(b.heightCm()) + "」→「" + fmtStr(a.getHeightCm()) + "」");
        }
        if (!eqStr(b.weightKg(), a.getWeightKg())) {
            lines.add("체중(kg): 「" + fmtStr(b.weightKg()) + "」→「" + fmtStr(a.getWeightKg()) + "」");
        }
        if (!eqStr(b.chiefComplaint(), a.getChiefComplaint())) {
            lines.add("주호소: 「" + fmtStr(b.chiefComplaint()) + "」→「" + fmtStr(a.getChiefComplaint()) + "」");
        }
        if (!eqStr(b.visitReason(), a.getVisitReason())) {
            lines.add("내원경위: 「" + fmtStr(b.visitReason()) + "」→「" + fmtStr(a.getVisitReason()) + "」");
        }
        if (!eqStr(b.historyPresentIllness(), a.getHistoryPresentIllness())) {
            lines.add("현병력: 「" + fmtStr(b.historyPresentIllness()) + "」→「" + fmtStr(a.getHistoryPresentIllness()) + "」");
        }
        if (!eqStr(b.pastHistory(), a.getPastHistory())) {
            lines.add("과거력: 「" + fmtStr(b.pastHistory()) + "」→「" + fmtStr(a.getPastHistory()) + "」");
        }
        if (!eqStr(b.familyHistory(), a.getFamilyHistory())) {
            lines.add("가족력: 「" + fmtStr(b.familyHistory()) + "」→「" + fmtStr(a.getFamilyHistory()) + "」");
        }
        if (!eqStr(b.allergy(), a.getAllergy())) {
            lines.add("알레르기: 「" + fmtStr(b.allergy()) + "」→「" + fmtStr(a.getAllergy()) + "」");
        }
        if (!eqStr(b.currentMedication(), a.getCurrentMedication())) {
            lines.add("복용약: 「" + fmtStr(b.currentMedication()) + "」→「" + fmtStr(a.getCurrentMedication()) + "」");
        }
        if (!eqStr(b.status(), a.getStatus())) {
            lines.add("상태: 「" + fmtStr(b.status()) + "」→「" + fmtStr(a.getStatus()) + "」");
        }
        if (lines.isEmpty()) {
            return "변경된 입력 항목 없음";
        }
        String joined = String.join("\n", lines);
        if (joined.length() <= MAX_LEN) {
            return joined;
        }
        return joined.substring(0, MAX_LEN) + "\n...(이하 생략)";
    }

    private static boolean eqLdT(LocalDateTime x, LocalDateTime y) {
        if (x == null && y == null) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.withNano(0).equals(y.withNano(0));
    }

    private static boolean eqLong(Long x, Long y) {
        if (x == null && y == null) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }

    private static boolean eqInt(Integer x, Integer y) {
        if (x == null && y == null) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }

    private static boolean eqBd(BigDecimal x, BigDecimal y) {
        if (x == null && y == null) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.compareTo(y) == 0;
    }

    private static boolean eqStr(String x, String y) {
        String a = x == null ? "" : x.trim();
        String b = y == null ? "" : y.trim();
        return a.equals(b);
    }

    private static String fmtDateTime(LocalDateTime v) {
        if (v == null) {
            return "(비어 있음)";
        }
        return v.withNano(0).format(DT);
    }

    private static String fmtLong(Long v) {
        if (v == null) {
            return "(비어 있음)";
        }
        return String.valueOf(v);
    }

    private static String fmtInt(Integer v) {
        if (v == null) {
            return "(비어 있음)";
        }
        return String.valueOf(v);
    }

    private static String fmtBd(BigDecimal v) {
        if (v == null) {
            return "(비어 있음)";
        }
        return v.stripTrailingZeros().toPlainString();
    }

    private static String fmtStr(String v) {
        if (v == null || v.isBlank()) {
            return "(비어 있음)";
        }
        String t = v.trim();
        if (t.length() > 200) {
            return t.substring(0, 200) + "…";
        }
        return t;
    }
}
