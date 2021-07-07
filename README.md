# SPRING COMMON USING LIB

### Main functions supporting
- Create criteria from params on requestm customize for normal and simple query
This function base on https://www.baeldung.com/rest-api-query-search-language-more-operations
Function query through specification supports One to one, one to many relation in model
- Automatically copy value from request object into model object
This function will ignore null value, this function will automatically create object based on type of relationship
- Database interceptor 

### Instruction 
#### I. Automatically copy value from request object into model object
- Step 1: Create request class
```Java
public class ProjectRequest {
 @NotNull
 @Size(min = 1, max = 255)
 private String name;

 private List<Long> taskManagerIds;

 @NotNull(message = "Invalid project manager")
 private Long projectManagerId;
}
```
- Step 2: Create model class
```Java
public class Project {
    public Project(long id) {this.id = id;}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name="pm_admin_seq")
    private User projectManager;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_rel_admin_project",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "admin_id"))
    @JsonManagedReference
    private List<User> taskManagers;
    
    private String name;
}
```
- Step 3: In service file, we just need to call method from Utils in lib, this method will copy value automatically with primitive or java original type into corresponding in 
entity object. With relationship field, it requires declaring field name follow the rule:
  + OneToOne or OneToMany relationship: field-name-in-request-class = field-name-in-entity-class + "Id"
  + ManyToMany: field-name-in-request-class = field-name-in-entity-class + "Ids"
  + Primary key always must be "id"
```Java
import com.conght.common.requestcriteria.util.RequestPropertiesUtil;

@Service
public class ProjectServiceImpl implements ProjectService {
        @Override
    public Project update(long id, ProjectRequest projectRequest) {
        Optional<Project> result = this.show(id);
        if (result.isPresent()) {
            Project project = result.get();
            RequestPropertiesUtil.copyNonNullProperties(projectRequest, project);
            return this.projectRepository.save(project);
        }
        throw new ItemNotFound("Project Not Found Id:" + id);
    }
}
```

#### II. Support using basic select condition in request 
Example 1: if you want to using BETWEEN condition in SQL, passing this params into request, current version support 
Date and number datatype
```
{
"startDt": ":2021-05-21 00:00:00|2021-05-23 00:00:00",
"estHour": ":2|3"
}
```
or
```
{
"startDt": "2021-05-23 00:00:00",
"estHour": "3"
}
```

In Request object, we can use annotation to validate input
```java
    @ValidDateOrDateRange
    private String startDt;
    @ValidNumberOrNumberRange
    private String estHour;
```

#### III. Basic Rest Exception Handler
Create a class to extends BaseRestExceptionHandler class
```java
import com.conght.common.exception.handler.BaseRestExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends BaseRestExceptionHandler {
}
```
Example response:
```json
{
  "apierror": {
    "status": "BAD_REQUEST",
    "timestamp": "07-07-2021 10:12:32",
    "message": "Param invalid",
    "debugMessage": null,
    "subErrors": [
      {
        "object": "issueIndexRequest",
        "field": "startDt",
        "rejectedValue": "2021-05-23 00:00:00a",
        "message": "must match \"^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$|^:(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\|(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$\""
      }
    ]
  }
}
```

#### IV. Database changes interceptor 
1. Class entity model extend base model
   public class Issue extends BaseModel 
   
2. Create custom EntityAuditLogInterceptor extend BaseEntityAuditLogInterceptor from library then
implement two method getUser and writeLog by yourself follows specific requirement in project

```java
import com.conght.common.database.interceptor.BaseEntityAuditLogInterceptor;
import com.conght.common.database.interceptor.model.BaseLogEntityChange;
import com.conght.common.database.interceptor.model.UserInfoLog;
import lombok.val;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EntityAuditLogInterceptor extends BaseEntityAuditLogInterceptor {
@Override
public UserInfoLog getUser() {
Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            User mUser = ((User)principal);
            return new UserInfoLog(mUser.getId(), mUser.getUsername());
        }
        return new UserInfoLog(0L, "");
    }

    @Override
    public void writeLog(BaseLogEntityChange baseLogEntityChange) {
        val env = SpringContextUtil.getApplicationContext().getBean(Environment.class);
        if(!env.getProperty("interceptor.database.status", "enable").equals("enable"))
            return;
        val logRepository = SpringContextUtil.getApplicationContext().getBean(env.getProperty("interceptor.database.repository.prefix", "log")
                + baseLogEntityChange.getEntityName() + env.getProperty("interceptor.database.repository.suffix", "Repository"));
        try {
            Class<?> cls = Class.forName(env.getProperty("interceptor.database.model.prefix", "Log") + baseLogEntityChange.getEntityName());
            Method insertLog = logRepository.getClass().getMethod("insert", Object.class);
            Object mLog = cls.getConstructor(BaseLogEntityChange.class).newInstance(baseLogEntityChange);
            insertLog.invoke(logRepository, mLog);
        } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
```

   + **(Optional)** In this case, function trying to writeLog to mongoDB. This following code handle this function
    
3. **(Optional)** Add these env into application.properties
```properties
interceptor.database.repository.prefix=log
interceptor.database.repository.suffix=Repository
interceptor.database.model.prefix=com.appdr.sradmin.mongo.model.Log
interceptor.database.status=enable
```
4. **(Optional)** Create Log model and Log repository (which implements MongoRepository)
+ Model:
```java
package com.appdr.sradmin.mongo.model;

import com.conght.common.database.interceptor.model.BaseLogEntityChange;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("LogIssue")
public class LogIssue extends BaseLogEntityChange {
    public LogIssue(BaseLogEntityChange baseLogEntityChange) {
        super(baseLogEntityChange.getEntityName(), baseLogEntityChange.getEntityId(),
                baseLogEntityChange.getUserName(), baseLogEntityChange.getUserId());
        this.setChanges(baseLogEntityChange.getChanges());
    }
}
```

+ MongoDb repository
```java
package com.appdr.sradmin.mongo.repository;

import com.appdr.sradmin.mongo.model.LogIssue;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogIssueRepository extends MongoRepository<LogIssue, String> {
}
```