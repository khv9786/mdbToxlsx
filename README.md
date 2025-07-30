# MDB 파일 데이터 카운터

이 프로젝트는 특정 디렉토리 내의 MDB 파일들을 읽어서 각 테이블의 컬럼별 데이터 개수를 세고 Excel 파일로 내보내는 Java 애플리케이션입니다.

## 기능

1. **MDB 파일 스캔**: 지정된 디렉토리에서 모든 .mdb 파일을 찾습니다.
2. **데이터 분석**: 각 MDB 파일의 모든 테이블과 컬럼을 분석합니다.
3. **데이터 카운트**: 각 컬럼에 있는 데이터의 개수를 세어줍니다.
4. **Excel 내보내기**: 분석 결과를 Excel 파일(.xlsx)로 내보냅니다.

## 요구사항

- Java 11 이상
- Maven 3.6 이상

## 설치 및 실행

### 1. 프로젝트 빌드

```bash
mvn clean compile
```

### 2. 실행

```bash
mvn exec:java -Dexec.mainClass="com.mdbcounter.Main"
```

또는 JAR 파일로 빌드 후 실행:

```bash
mvn clean package
java -jar target/mdb-counter-1.0-SNAPSHOT.jar
```

### 3. 사용법

1. 프로그램 실행 시 MDB 파일이 있는 디렉토리 경로를 입력합니다.
2. 프로그램이 자동으로 모든 .mdb 파일을 스캔하고 분석합니다.
3. 분석이 완료되면 `mdb_data_count_[timestamp].xlsx` 파일이 생성됩니다.

## 출력 파일 형식

생성되는 Excel 파일에는 다음 정보가 포함됩니다:

| 컬럼 | 설명 |
|------|------|
| 파일명 | MDB 파일의 이름 |
| 테이블명 | 테이블의 이름 |
| 컬럼명 | 컬럼의 이름 |
| 데이터 개수 | 해당 컬럼에 있는 데이터의 개수 |

## 의존성

- **UCanAccess**: MDB 파일 읽기
- **Apache POI**: Excel 파일 생성
- **PostgreSQL**: 데이터베이스 연결
- **SLF4J + Logback**: 로깅

## 로그

프로그램 실행 시 로그는 다음 위치에 저장됩니다:
- 콘솔 출력
- `logs/mdb-counter.log` 파일

## PostgreSQL 설정

### 1. 데이터베이스 설정 파일 수정

`src/main/resources/database.properties` 파일을 열어서 다음 정보를 수정하세요:

```properties
db.url=jdbc:postgresql://localhost:5432/your_database_name
db.username=your_username
db.password=your_password
```

### 2. 데이터베이스 연결 테스트

데이터베이스 연결을 테스트하려면 다음 명령을 실행하세요:

```bash
mvn exec:java -Dexec.mainClass="com.mdbcounter.util.DatabaseTest"
```

### 3. PostgreSQL 설치 및 설정

1. PostgreSQL을 설치하세요: https://www.postgresql.org/download/
2. 데이터베이스를 생성하세요:
   ```sql
   CREATE DATABASE your_database_name;
   ```
3. 사용자를 생성하고 권한을 부여하세요:
   ```sql
   CREATE USER your_username WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE your_database_name TO your_username;
   ```

## 주의사항

- MDB 파일에 접근할 수 있는 권한이 필요합니다.
- PostgreSQL 서버가 실행 중이어야 합니다.
- 데이터베이스 연결 정보가 올바르게 설정되어야 합니다.
- 큰 MDB 파일의 경우 처리 시간이 오래 걸릴 수 있습니다.
- Excel 파일은 프로그램이 실행된 디렉토리에 생성됩니다.

## 문제 해결

### MDB 파일을 읽을 수 없는 경우
- 파일 경로가 올바른지 확인하세요.
- 파일에 대한 읽기 권한이 있는지 확인하세요.
- 파일이 손상되지 않았는지 확인하세요.

### Excel 파일이 생성되지 않는 경우
- 디렉토리에 쓰기 권한이 있는지 확인하세요.
- 디스크 공간이 충분한지 확인하세요. 

### UCanAccess 관련 경고/에러가 계속 출력되는 경우

- UCanAccess 드라이버는 버전에 따라 함수 자동 로딩 관련 경고/에러가 발생할 수 있습니다.
- 아래와 같이 **JVM 옵션**을 추가하면 대부분의 경고/에러 메시지를 억제할 수 있습니다.

```
-Ducanaccess.disableAutoLoadFunctions=true
-Ducanaccess.disableAutoLoadingFunctions=true
```

- 인텔리제이/명령행에서 실행 시 VM 옵션에 위 값을 추가하세요.
- 예시: 인텔리제이 Run/Debug Configurations > VM options

--김현빈