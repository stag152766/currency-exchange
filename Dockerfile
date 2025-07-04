# Use lightweight JRE with Tomcat
FROM tomcat:10.1-jre21

# Remove default webapps for a clean deployment
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR file to Tomcat webapps directory
COPY target/currency-exchange.war /usr/local/tomcat/webapps/ROOT.war

# Copy the database file to Tomcat directory
COPY src/main/resources/exchange1.db /usr/local/tomcat/exchange1.db

# Expose port 8080
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"] 