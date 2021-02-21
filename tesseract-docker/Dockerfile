FROM adoptopenjdk:11-jre-hotspot

RUN apt-get update -y

# Install tesseract library
RUN apt install tesseract-ocr -y

# Download last language package
RUN mkdir -p /usr/share/tessdata
ADD https://github.com/tesseract-ocr/tessdata/raw/master/tur.traineddata /usr/share/tessdata/tur.traineddata

# Check the installation status tesseract
RUN tesseract --list-langs
RUN tesseract -v