FROM python:3.10.8-slim@sha256:49749648f4426b31b20fca55ad854caa55ff59dc604f2f76b57d814e0a47c181 as base

FROM base as builder

RUN apt-get -qq update \
    && apt-get install -y --no-install-recommends \
        wget g++ \
    && rm -rf /var/lib/apt/lists/*

ENV GRPC_HEALTH_PROBE_VERSION=v0.4.18
RUN wget -qO/bin/grpc_health_probe https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/${GRPC_HEALTH_PROBE_VERSION}/grpc_health_probe-linux-amd64 && \
    chmod +x /bin/grpc_health_probe

COPY requirements.txt .
RUN pip install -r requirements.txt

FROM base as without-grpc-health-probe-bin

ENV PYTHONUNBUFFERED=1

ENV ENABLE_PROFILER=1

WORKDIR /email_server

COPY --from=builder /usr/local/lib/python3.10/ /usr/local/lib/python3.10/

COPY . .

EXPOSE 5000
ENTRYPOINT [ "python", "email_server.py" ]

FROM without-grpc-health-probe-bin

COPY --from=builder /bin/grpc_health_probe /bin/grpc_health_probe