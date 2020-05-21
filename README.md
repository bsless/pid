# PID

Implementation of PID controller in Clojure (and P, PI, PD).

## Usage

```clojure
(require '[pid.core :as pid])
(def controller (pid/pid-controller target dt p i d))
(pid/-step controller signal)
;;; run simulation
(def log (pid/simulate controller f tolerance steps init))
```

## License

Copyright © 2020 Ben Sless

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
