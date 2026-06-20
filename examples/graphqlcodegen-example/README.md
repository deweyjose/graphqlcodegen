# graphqlcodegen-example
This repo provides a simple example project that uses graphqlcodegen. 
Start the server and run the client to see it in action.

## Clone the repo
```console
git clone https://github.com/deweyjose/graphqlcodegen-example.git
cd graphqlcodegen-example
```

## Start the server
```console
$ cd server 
$ mvn spring-boot:run
```

## Run the client
This will query the server for a list of shows and print them to the console.
```console
$ cd ../client
$ mvn spring-boot:run
{shows=[{id=1, title=Stranger Things}, {id=2, title=Ozark}, {id=3, title=The Crown}, {id=4, title=Dead to Me}, {id=5, title=Orange is the New Black}]}

```
