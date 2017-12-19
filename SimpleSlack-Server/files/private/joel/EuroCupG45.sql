BEGIN
FOR I IN (SELECT * FROM tabs) LOOP
EXECUTE IMMEDIATE ('drop table' || i.table_name || 'cascade constraints');
END LOOP;
END;
/