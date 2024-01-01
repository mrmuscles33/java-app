CREATE FUNCTION TO_CHAR (@date datetime, @format varchar(20))
RETURNS varchar(20) AS
BEGIN
    DECLARE @result varchar(20);
    SET @result = FORMAT(@date, @format);
    RETURN @result;
END;
