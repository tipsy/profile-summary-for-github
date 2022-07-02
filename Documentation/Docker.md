
# Docker

*How to use this project with docker.*

<br>

## Building

*How to compile the source code.*

<br>

-   Clone the repository

    ```shell
    git clone https://github.com/tipsy/profile-summary-for-github.git
    ```
    
-   Navigate to the root folder

    ```shell
    cd profile-summary-for-github
    ```
    
-   Compile the source code

    ```shell
    docker build -t profile-summary-for-github
    ```
    
<br>
<br>

## Running

*How to start the compiled project.*

<br>

### Without Token

```shell
docker run                              \
    -it                                 \
    --rm                                \
    --name profile-summary-for-github   \
    -p 7070:7070 profile-summary-for-github
```

<br>

### With Token

```shell
docker run                              \
    -it                                 \
    --rm                                \
    --name profile-summary-for-github   \
    -p 7070:7070                        \
    -e "API_TOKENS=<My Token A>,<My Token B>" profile-summary-for-github
```

<br>

### Interface

You can access it at http://localhost:7070

<br>
