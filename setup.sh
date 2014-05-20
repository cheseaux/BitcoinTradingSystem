cd commons;
sbt update
sbt eclipse
sbt compile;
sbt package;
sbt publish;
cd ..

cd crawl-framework
sbt update
sbt eclipse
sbt compile;
cd ..

cd reactive-stocks
sbt update
sbt eclipse
sbt compile;
cd ..

