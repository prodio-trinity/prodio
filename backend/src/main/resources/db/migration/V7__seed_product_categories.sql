-- 대분류 6종
INSERT INTO catalog_product_categories (category_code, category_name, parent_id)
VALUES
    ('INPUT', '입력장치', NULL),
    ('DISPLAY', '디스플레이', NULL),
    ('STORAGE', '저장장치', NULL),
    ('COMPUTING', '컴퓨팅 부품', NULL),
    ('AUDIO', '음향기기', NULL),
    ('ACCESSORY', '액세서리', NULL);

-- 소분류
INSERT INTO catalog_product_categories (category_code, category_name, parent_id)
SELECT v.code, v.name, p.id
FROM (VALUES
          ('MOUSE', '마우스', 'INPUT'),
          ('KEYBOARD', '키보드', 'INPUT'),
          ('GAMEPAD', '게임패드/조이스틱', 'INPUT'),
          ('TABLET_PEN', '펜타블렛', 'INPUT'),
          ('WEBCAM', '웹캠', 'INPUT'),
          ('MONITOR', '모니터', 'DISPLAY'),
          ('MONITOR_ARM', '모니터암', 'DISPLAY'),
          ('SSD', 'SSD', 'STORAGE'),
          ('HDD', 'HDD', 'STORAGE'),
          ('EXT_STORAGE', '외장저장장치', 'STORAGE'),
          ('NAS', 'NAS', 'STORAGE'),
          ('CPU', 'CPU', 'COMPUTING'),
          ('GPU', '그래픽카드', 'COMPUTING'),
          ('MAINBOARD', '메인보드', 'COMPUTING'),
          ('RAM', '메모리(RAM)', 'COMPUTING'),
          ('PSU', '파워서플라이', 'COMPUTING'),
          ('COOLING', '쿨링', 'COMPUTING'),
          ('CASE', '케이스', 'COMPUTING'),
          ('SPEAKER', '스피커', 'AUDIO'),
          ('HEADSET', '헤드셋', 'AUDIO'),
          ('MIC', '마이크', 'AUDIO'),
          ('CABLE', '케이블', 'ACCESSORY'),
          ('POUCH', '파우치/케이스류', 'ACCESSORY'),
          ('STAND', '거치대', 'ACCESSORY'),
          ('HUB_MULTITAP', '허브/멀티탭', 'ACCESSORY'),
          ('ACC_ETC', '기타액세서리', 'ACCESSORY')
     ) AS v(code, name, parent_code)
         JOIN catalog_product_categories p ON p.category_code = v.parent_code;
