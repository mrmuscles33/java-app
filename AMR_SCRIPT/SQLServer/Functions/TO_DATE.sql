CREATE FUNCTION TO_DATE (@str varchar(20), @format varchar(20))
RETURNS datetime AS
BEGIN
    DECLARE @result datetime;
    SET @result = CAST(@str AS DATETIME);
    RETURN @result;
END;
