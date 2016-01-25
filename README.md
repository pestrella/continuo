# continuo

_continuo_ is a library that generates unique, sequenced numbers.

## Usage

Generating numbers that are unique on one host:

```
user> (gen-id)
1724978258329521153
```

To generate numbers that are unique across a network, you should
provide an instance ID for each host.

```
user> (binding [*instance-id* 112]
        (gen-id))
1724978342937021808
```

## License

Copyright © 2016 Paolo Estrella.

Distributed under the GNU General Public License, version 3.
