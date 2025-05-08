#!/bin/bash

# Copy template files to actual configuration files
cp src/main/resources/application.yml.template src/main/resources/application.yml
cp src/main/resources/application-dev.yml.template src/main/resources/application-dev.yml
cp src/main/resources/application-prod.yml.template src/main/resources/application-prod.yml

echo "Configuration files have been created. Please update the credentials and settings as needed."
