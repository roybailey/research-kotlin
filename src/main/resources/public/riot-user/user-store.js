// UserStore definition.
// Flux stores house application logic and state that relate to a specific domain.
function UserStore() {
  riot.observable(this) // Riot provides our event emitter.

  var self = this

  self.state = {
    isLoaded: false,
    users: []
  }

  // Our store's event handlers / API.
  // This is where we would use AJAX calls to interface with the server.
  // Any number of views can emit actions/events without knowing the specifics of the back-end.
  // This store can easily be swapped for another, while the view components remain untouched.

  self.on('users_add', function(newUser) {
    fetch("/users",
    {
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        method: "POST",
        body: JSON.stringify(newUser)
    })
    .then(function (response) {
        console.log(response);
        return response.json();
    }).then(function(json) {
        self.state.users.push(json)
        self.trigger('users_changed', self.state)
    })
    .catch(function (ex) {
        console.log('failed to save user', ex);
    })
  })

  self.on('users_remove', function(users) {
    users.forEach(function (user) {
      console.log(`removing ${JSON.stringify(user)}`)
      fetch("/users/"+user.id,
      {
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
          },
          method: "DELETE"
      })
      .then(function (response) {
          console.log(response);
          return response.text();
      }).then(function(userId) {
          self.state.users = self.state.users.filter(function (it) { return it.id != userId })
          console.log(self.state.users)
          self.trigger('users_changed', self.state)
      })
      .catch(function (ex) {
          console.log('failed to save user', ex);
      })

    })
    self.trigger('users_changed', self.state)
  })

  self.on('users_init', function() {
    fetch('/users')
            .then(function (response) {
                console.log(response);
                return response.json();
            }).then(function (json) {
                self.state = {
                    isLoaded: true,
                    users: Object.keys(json).map(function (it) { return Object.assign(json[it],{hidden:false})})
                }
                console.log('LOADED!')
                self.trigger('users_changed', self.state)
            })
            .catch(function (ex) {
                console.log('failed to get users', ex);
            })

  })
  // The store emits change events to any listening views, so that they may react and redraw themselves.
}
