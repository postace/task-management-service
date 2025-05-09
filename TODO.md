
- check db to see if data is correctly saved
- validate business fields in bugDto and featureDto
- update bug is not okay, we need to separate these model for update instead
- Change to use CreateUserRequest, UpdateUserRequest
- Add error code for the front end to proper parse and handle it
- do we need to add something like requestId for proper tracing??

- Does the hibernate join will slow us down our application???
- A lot of @Transactional, do we really need them??