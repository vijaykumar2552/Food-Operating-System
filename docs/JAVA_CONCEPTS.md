# Java concepts demonstrated

- Encapsulation: model fields are private and exposed through controlled accessors.
- Inheritance: domain exceptions share the `AppException` base type.
- Polymorphism: `UserDao` is implemented by `JdbcUserDao` and can be replaced in tests.
- Abstraction: service and DAO contracts separate business rules from JDBC details.
- Collections: DAO results and service-level item lists will use typed Java collections as features expand.
- Exception handling: checked application exceptions translate technical failures into user-safe messages.
- Multithreading: `SwingWorker` isolates slow database work from the Swing event-dispatch thread.
