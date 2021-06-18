# SPRING COMMON USING LIB

### Main functions supporting
- Create criteria from params on requestm customize for normal and simple query
This function base on https://www.baeldung.com/rest-api-query-search-language-more-operations
Function query through specification supports One to one, one to many relation in model

- Automatically copy value from request object into model object
This function will ignore null value and if property of Model is OneToOne Relationship, this function will automatically create object based on type of relationship
  
Database interceptor instruction setup:
- Extending class BaseEntityAuditLogInterceptor and implement your own method writeLog()
- Model needs to extend com.conght.common.BaseModel to start logging. This class will use variable
isCreate in BaseModel class to identify create or update function. 
