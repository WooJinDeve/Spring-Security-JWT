## 🍃 스프링 부트 JWT 로그인
 
 ### 기본 설정
- **스프링 시작**
    - [https://start.spring.io/](https://start.spring.io/)


- **Spring Package Setting**
    - `Java Version` : 11
    - `Project` : `Gradle Project`
    - `Packaging`
        - `Spring Boot` : `Jar`
    - `Dependencies`
        - `Spring Web`, `Lombok`, `Spring DATA JPA`, `MySQL Driver`, `Spring Security`, "Jwt"

### 세션(Session)

- 클라이언트가 최초 요청을 하면 서버는 `html header`에 `세션 ID`가 담긴 응답 데이터를 전달한다.
    - 서버는 이 `세션 ID`를 저장하고 있음.
- 클라이언트는 재방문시 쿠키에 있는 세션정보와 함께 서버에 요청을 한다
- 서버는 클라이언트의 `세션 ID`와 저장된 서버 `세션 ID`를 비교해 재 방문여부를 체크한다.
- **세선 만료**
    - 서버에서 `세션 ID`를 삭제할 경우
    - 클라이언트가 브라우저를 종료할 경우 이때 서버의 세션은 만료되지 않음.
        - 이때 세션의 만료시간을 설정해야함 ( 보통 30분 )

### CIA

- **기밀성 ( Confidentiality )**
    - 정보를 오직 인가된 사용자에게만 허가
    - **기밀성 훼손**
        - 업데이트 서버 해킹 : 정상적인 관리자가 아닌 외부 해커에 의해 서버 접근.
        - 관리자 PC 해킹 : 데이터베이스 접근 ID와 암호 노출.
        - 내부의 자산을 파악하기 위해 훔쳐보는 스캐닝 같은 행위.
        - 네트워크 통신 경로의 중간에 개입하여 도청하는 행위
- **무결성 ( Integrity )**
    - 부적절한 정보 변경이나 파기 없이 정확하고 완전하게 보존
    - **무결성 훼손**
        - 정상적인 업데이트 파일이 아닌 악성코드가 포함된 파일로 교체된 것은 무결성 위배.
- **가용성 ( Availability )**
    - 시기적절하면서 신뢰할 수 있는 정보로 접근과 사용.
    - **가용성 훼손**
        - 개인정보 유출 사고만 해당하기 때문에 가용성의 위배는 발생하지 않음.
        - 가용성 훼손의 대표적 공격은 서비스 거부 ( `Dos` ) & 분산 서비스 거부 ( `DDos` )
            - 과부하로인해 시스템을 사용 불가능하게 만든다

### RSA 암호 알고리즘 방식

1. `A`가 `B`에게 정보를 안전하게 보내고 싶어한다. 이때 `RSA` 알고리즘을 이용하고자 한다.
2. `B`가 공개키와 개인키를 만들어 `A`에게 공개키를 보낸다. (개인키는 `B`만 가지고 있다.)
3. `A`가 `B`로부터 받은공개키를 이용하여 보낼 정보를 암호화한다.
4. `A`가 암호화된 정보를 `B`에게 보낸다
5. `B`가 암호화된 정보를 받고개인키를 이용하여 암호를 해독한다.

### JWT 구성 요소

- **Header** : `Signature`를 해싱하기 위한 알고리즘 정보
    - 토큰의 유형, 알고리즘 정보
- **Payload** : 서버와 클라이언트가 주고 받는, 시스템에서 실제로 사용될 정보에 대한 내용
    - **등록된 클레임** :  미리 정의된 클레임의 집합 ( `iss(발행자)`, `exp(만료 시간)`, `sub(주체)`, `aub(청중) 및 기타` )
    - **개인 클래임** : 정보를 공유하기 위해 생성된 사용자 지정 클레임

```json
{
	"sub" : "123456789"
	"name" : "John"
	"admin" : true
}
```

- **Signature** : 토큰의 유효성을 가진 문자열
    - `HMAC SHA256` 알고리즘을 사용함.
        - `HMAC` : 시크릿 키를 포함
        - `SHA256` : `Hash`

### JWT 장점

- 중앙의 인증서버, 데이터 스토어에 대한 의존성 없이 없으며, 시스템 확장에 유리함
- **Base64 URL safe Encoding** : `URL`. `Cookie`, `Header` 모두 사용 가능
    - 암호화, 복호화 가능

### JWT 단점

- `Payload`의 정보가 많아지면 네트워크 사용량 증가, 데이터 설계 고려 필요
- 토큰이 클라이언트에 저장되기 때문에 서버에서 클라이언트의 토큰을 조작할 수 없음.

### 스프링 필터 적용

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        // 세션 사용 X
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(corsConfig.corsFilter()) // 시큐리티 필터에 등록 인증
                //form, http 로그인 X
                .formLogin().disable()
                .httpBasic().disable()
                .authorizeRequests()
                .antMatchers("/api/v1/user/**")
                .access("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
                .antMatchers("/api/v1/manager/**")
                .access("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
                .antMatchers("/api/v1/admin/**")
                .access("hasRole('ROLE_ADMIN')")
                .anyRequest().permitAll();
        return http.build();
    }
}
```

- `http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)`
    - 스프링 시큐리티는 `StateFull`을 기본값으로 지정한다. `JWT`는 `Default`로 `StateLess` 방식을 사용
- `formLogin().disable()` : `Form` 로그인 방식을 사용하지 않음
- `httpBasic().disable()` : `http` 요청시 `ID`, `PW` 등을 함께 요청을 기본 값으로 지정
    - `JWT` 사용시 `ID`와 `PW` 대신 `Token`을 통한 요청을 위해 `disable()`

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); 
        config.addAllowedOrigin("*");     // 모든 아이피의 응답 허용
        config.addAllowedHeader("*");     // 모든 header 응답 허용
        config.addAllowedMethod("*");     // 모든 Method 요청 하용

        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
```

- `httpBasic().disable()` : 서버 응답시 `json`을 자바스크립트에서 처리할 수 있게 할지 설정
- `config.addAllowedOrigin("*");` : 모든 아이피의 응답 허용
- `config.addAllowedHeader("*");` : 모든 `header` 응답 허용
- `config.addAllowedMethod("*");` : 모든 `Method` 요청 하용

### 필터 우선순위

- 스프링에서는 클라이언트에서 `Request`가 오면 `filtering`을 할 수 있는 기능을 지원해준다.
- 스프링 `filter`  보다 스프링 시큐리티 `filterChain`이 높은 우선순위를 가지게 된다.
    - 스프링 시큐리티의 `filterchain` 후 스프링 `filter`가 작동한다.

### 커스텀 필터 등록

```java
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        ObjectMapper om = new ObjectMapper();
        LoginRequestDto loginRequestDto = null;
        try {
            loginRequestDto = om.readValue(request.getInputStream(), LoginRequestDto.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword());

        Authentication authentication =
                authenticationManager.authenticate(authenticationToken);
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        log.info("user {}",principal.getUser());
        return authentication;
    }
}
```

- `Spring Security`의 `UserDetailsService` 는 `/login` 요청시 실행되는 함수이다. 하지만 `formLogin.disable()`을 통해 해당 URL에 대한 요청을 할 수 없으므로 커스텀 필터를 등록 하였다.
- `UsernamePasswordAuthenticationToken` : `Username`, `Password`를 통해 발급하능한 토큰이다
    - 토큰을 통해 유저 정보를 조회할 수 있다. `FormLogin` 시 자동으로 실행된다.
- `authenticate(authenticationToken);`
    - 실행시 인증 프로바이더가 유저 디테일 서비스의  `loadUserByUsername()` 를 호출
    - `UserDetails`를 리턴받아서 토큰의 두번째 파라메터 `(credential)`과 `UserDetails(DB값)`의 `getPassword()`함수로 비교
    - 일치하면 `Authentication` 객체를 만들어서 필터체인으로 리턴해준다

```java
@Override
protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult)
        throws IOException, ServletException {
    PrincipalDetails principalDetailis = (PrincipalDetails) authResult.getPrincipal();

    String jwtToken = JWT.create()
            .withSubject(principalDetailis.getUsername())
            .withExpiresAt(new Date(System.currentTimeMillis()+JwtProperties.EXPIRATION_TIME))
            .withClaim("id", principalDetailis.getUser().getId())
            .withClaim("username", principalDetailis.getUser().getUsername())
            .sign(Algorithm.HMAC512(JwtProperties.SECRET));

    response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX+jwtToken)
}
```

- `attemptAuthentication` 인증이 정상적으로 되었으면 `successfulAuthentication` 함수 실행
- `JWT.create()` : 토큰 생성
