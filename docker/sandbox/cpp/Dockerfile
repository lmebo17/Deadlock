FROM gcc:13
RUN useradd -m -u 1000 sandbox
COPY runner.sh /runner.sh
RUN chmod +x /runner.sh
USER sandbox
ENTRYPOINT ["/runner.sh"]
