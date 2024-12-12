FROM mcr.microsoft.com/dotnet/sdk:7.0.302@sha256:5c638e77052b5ae4f6f1da3885035b510fc379d2ce4be274c70679114bcdb936 as builder
WORKDIR /app
COPY cartservice.csproj .
RUN dotnet restore cartservice.csproj -r linux-musl-x64
COPY . .
RUN dotnet publish cartservice.csproj -p:PublishSingleFile=true -r linux-musl-x64 --self-contained true -p:PublishTrimmed=True -p:TrimMode=Link -c release -o /cartservice --no-restore

FROM mcr.microsoft.com/dotnet/runtime-deps:7.0.4-alpine3.16-amd64@sha256:7141eea9c7be5f4d2f09df427ba37620e50be150fc93015288b3e26c5071af81 as without-grpc-health-probe-bin

WORKDIR /app
COPY --from=builder /cartservice .
EXPOSE 7070
ENV DOTNET_EnableDiagnostics=0 \
    ASPNETCORE_URLS=http://*:7070
USER 1000
ENTRYPOINT ["/app/cartservice"]

FROM without-grpc-health-probe-bin
USER root

ENV GRPC_HEALTH_PROBE_VERSION=v0.4.18
RUN wget -qO/bin/grpc_health_probe https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/${GRPC_HEALTH_PROBE_VERSION}/grpc_health_probe-linux-amd64 && \
    chmod +x /bin/grpc_health_probe
USER 1000