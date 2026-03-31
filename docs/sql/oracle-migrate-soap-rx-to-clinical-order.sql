DECLARE
  v_order_id NUMBER(19);
BEGIN
  FOR r IN (
    SELECT rx.PRESCRIPTION_ID,
           rx.VISIT_ID,
           rx.MEDICATION_NAME,
           rx.DOSAGE,
           rx.DAYS,
           rx.CREATED_AT,
           rx.UPDATED_AT
    FROM VISIT_SOAP_PRESCRIPTION rx
    WHERE NOT EXISTS (
      SELECT 1 FROM CLINICAL_ORDER co WHERE co.LEGACY_PRESCRIPTION_ID = rx.PRESCRIPTION_ID
    )
  ) LOOP
    INSERT INTO CLINICAL_ORDER (
      ORDER_ID,
      VISIT_ID,
      ORDER_TYPE,
      ORDER_STATUS,
      DOCTOR_ID,
      ORDER_DATE,
      CREATED_AT,
      UPDATED_AT,
      LEGACY_PRESCRIPTION_ID
    ) VALUES (
      CL_ORDER_SEQ.NEXTVAL,
      r.VISIT_ID,
      'PRESCRIPTION',
      'REQUESTED',
      NULL,
      r.CREATED_AT,
      r.CREATED_AT,
      r.UPDATED_AT,
      r.PRESCRIPTION_ID
    )
    RETURNING ORDER_ID INTO v_order_id;

    INSERT INTO CLINICAL_ORDER_ITEM (
      ORDER_ITEM_ID,
      ORDER_ID,
      ITEM_CODE,
      ITEM_NAME,
      ITEM_DOSAGE,
      DOSE,
      FREQUENCY,
      DURATION,
      CREATED_AT
    ) VALUES (
      CL_ORDER_ITEM_SEQ.NEXTVAL,
      v_order_id,
      NULL,
      r.MEDICATION_NAME,
      r.DOSAGE,
      NULL,
      NULL,
      r.DAYS,
      r.CREATED_AT
    );
  END LOOP;
  COMMIT;
END;
/
